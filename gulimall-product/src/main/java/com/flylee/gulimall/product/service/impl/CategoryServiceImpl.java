package com.flylee.gulimall.product.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.dao.CategoryDao;
import com.flylee.gulimall.product.entity.CategoryEntity;
import com.flylee.gulimall.product.service.CategoryBrandRelationService;
import com.flylee.gulimall.product.service.CategoryService;
import com.flylee.gulimall.product.vo.Catalog2VO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redisson;
    private final CategoryBrandRelationService relationService;

//    private Map<String, Map<String, List<Catalog2VO>>> catalogCacheMap = new HashMap<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        return categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0L)
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, categoryEntities)))
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort()))).collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public Long[] findCategoryPathById(Long categoryId) {
        List<Long> categoryPaths = new ArrayList<>();
        findPath(categoryId, categoryPaths);
        Collections.reverse(categoryPaths);
        return categoryPaths.toArray(new Long[categoryPaths.size()]);
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到哪个名字的缓存。【缓存的分区（按照业务类型分）】
     * 2、@Cacheable("category")
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     *      1）、如果缓存中有，方法不用调用
     *      2）、key默认自动生成，缓存的名字：SimpleKey[]（自主生成的key值），Redis中的键是value和key拼接起来的
     *      3）、缓存的value的值。默认使用JDK序列化机制，将序列化后的数据存到Redis
     *      4）、默认ttl时间 -1；
     *    自定义
     *      1）、指定生成的缓存使用的key
     *      2）、指定缓存数据的存活时间
     *      3）、将数据保存为JSON格式
     *          CacheAutoConfiguration
     *          自定义RedisCacheConfiguration即可
     * 4、Spring-Cache的不足
     *      1）、读模式：
     *          缓存穿透：查询一个null数据。解决：缓存空数据：cache-null-values
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁？默认是无加锁的；sync = true 加锁，解决击穿
     *          缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间：spring.cache.redis.time-to-live
     *      2）、写模式（缓存与数据库一致）
     *          1）、读写加锁
     *          2）、引入Canal，感知到MySQL的更新去更新数据库
     *          3）、读多写多，直接查询数据库
     *   总结：
     *      常规数据（读多写少，即时性，一致性要求不高的数据），完全可以使用Spring-Cache；写模式（只要缓存的数据有过期时间）
     *      特殊数据，特殊设计
     *
     *   原理：
     *      CacheManager(RedisCacheManager) -> Cache(RedisCache) -> Cache负责缓存的读写
     *
     *
     */
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("进入方法");
        return list(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getParentCid, 0));
    }

    @Override
    @Cacheable(value = "category", key = "#root.methodName")
    public Map<String, List<Catalog2VO>> getCatalogJson() {
//        本地缓存
//        String key = "catalogJson";
//        Map<String, List<Catalog2VO>> catalogMap = this.catalogCacheMap.get(key);
//        if (catalogMap == null) {
//            catalogMap = getCatalogJSONFromDB();
//            this.catalogCacheMap.put(key, catalogMap);
//        }
//
//        return catalogMap;

        // 给缓存中放JSON字符串，拿出的JSON字符串还能逆转为能用的对象类型：【序列化与反序列化】

        // 常见缓存问题的解决
        // 1、空结果缓存：解决缓存穿透
        // 2：设置过期时间（加随机值）：解决缓存雪崩
        // 3：加锁：解决缓存击穿

        // 1、加入缓存逻辑，缓存中存的数据是JSON字符串
        // JSON跨语言、跨平台兼容
        String catalogJSONStr = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSONStr)) {
            // 2、缓存中没有，查询数据库
            System.out.println(DateUtil.formatDateTime(new Date()) + "：缓存未命中...查询数据库...");
            Map<String, List<Catalog2VO>> catalogJSONFromDB = getCatalogJSONFromDBWithRedissonLock();
            // 3、查到的数据再放入缓存，将对象转为JSON字符串放入缓存
            redisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(catalogJSONFromDB), 1, TimeUnit.DAYS);
            return catalogJSONFromDB;
        }

        System.out.println(DateUtil.formatDateTime(new Date()) + "：缓存命中...直接返回...");
        return JSON.parseObject(catalogJSONStr, new TypeReference<Map<String, List<Catalog2VO>>>(){});
    }

    /**
     * 级联更新所有关联的数据
     * @CacheEvict：失效模式
     * 1、同时进行多种缓存操作 @Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category", allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存的前缀
     * @param category 分类
     */
    // 单个缓存失效
//    @CacheEvict(value = "category", key = "'getLevel1Categories'")
    // 多个缓存
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categories'"),
//            @CacheEvict(value = "category", key = "'getCatalogJSON'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    // 双写模式
    @CachePut
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        relationService.updateCategory(category.getCatId(), category.getName());

        // 同时删除缓存中的数据
        // 等待下次主动查询进行更新
        // redis.del("catalogJSON")
    }

    private Map<String, List<Catalog2VO>> getCatalogJSONFromDBWithRedissonLock() {
        RLock lock = redisson.getLock("CatalogJson-lock");
        // 该方法会阻塞其他线程向下执行，只有释放锁之后才会接着向下执行
        lock.lock();

        Map<String, List<Catalog2VO>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }

        return dataFromDB;
    }

    private Map<String, List<Catalog2VO>> getCatalogJSONFromDBWithRedisLock() {
        // 1、占分布式锁，去Redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lock)) {
            // 加锁成功... 执行业务
            System.out.println(DateUtil.formatDateTime(new Date()) + "：获取分布式锁成功....");

            // 2、设置过期时间，必须和加锁是同步的，原子的 -> 改为：Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
//            stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catalog2VO>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
                System.out.println(DateUtil.formatDateTime(new Date()) + "：删除分布式锁成功....");
            }

            // 获取值对比+对比成功删除=原子操作，Lua脚本解锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                // 删除自己的锁
//                redisTemplate.delete("lock");
//            }

            return dataFromDB;
        } else {
            // 加锁失败...重试，类似synchronized
            // 休眠100ms重试，自旋
            System.out.println(DateUtil.formatDateTime(new Date()) + "：获取分布式锁失败...等待重试");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 自旋的方式
            return getCatalogJSONFromDBWithLocalLock();
        }
    }

    private Map<String, List<Catalog2VO>> getDataFromDB() {
        // 得到锁之后，再去缓存确认一次，如果没有才需要继续查询
        String catalogJSONStr = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSONStr)) {
            // 缓存不为null直接返回
            return JSON.parseObject(catalogJSONStr, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        }

        System.out.println(DateUtil.formatDateTime(new Date()) + "：查询了数据库......");

        List<CategoryEntity> categoryEntities = list();

        // 1、查询所有一级分类
        List<CategoryEntity> level1Categories = getCategoriesByParentCid(categoryEntities, 0L);

        // 2、封装数据
        return level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 查询二级分类
            List<CategoryEntity> level2Categories = getCategoriesByParentCid(categoryEntities, v.getCatId());

            return level2Categories.stream().map(category2 -> {
                // 查询三级分类
                List<CategoryEntity> level3Categories = getCategoriesByParentCid(categoryEntities, category2.getCatId());
                List<Catalog2VO.Catalog3VO> catalog3VOList = level3Categories.stream()
                        .map(category3 -> new Catalog2VO.Catalog3VO(category2.getCatId().toString(), category3.getCatId().toString(), category3.getName()))
                        .collect(Collectors.toList());

                return new Catalog2VO(v.getCatId().toString(), catalog3VOList, category2.getCatId().toString(), category2.getName());
            }).collect(Collectors.toList());
        }));
    }

    private Map<String, List<Catalog2VO>> getCatalogJSONFromDBWithLocalLock() {
        synchronized (this) {
            // 得到锁之后，再去缓存确认一次，如果没有才需要继续查询
            return getDataFromDB();
        }
    }

    private List<CategoryEntity> getCategoriesByParentCid(List<CategoryEntity> categoryEntities, long parentCid) {
        return categoryEntities.stream().filter(c -> c.getParentCid() == parentCid).collect(Collectors.toList());
    }

    private void findPath(Long categoryId, List<Long> categoryPaths) {
        if (categoryId != 0) {
            categoryPaths.add(categoryId);
            CategoryEntity byId = getById(categoryId);
            findPath(byId.getParentCid(), categoryPaths);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> categoryEntities) {
        return categoryEntities.stream().filter(c -> c.getParentCid().equals(categoryEntity.getCatId()))
                .peek(eachCategoryEntity -> eachCategoryEntity.setChildren(getChildren(eachCategoryEntity, categoryEntities)))
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort()))).collect(Collectors.toList());
    }

}