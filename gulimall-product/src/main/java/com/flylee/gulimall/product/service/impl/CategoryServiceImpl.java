package com.flylee.gulimall.product.service.impl;

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
import com.flylee.gulimall.product.service.CategoryService;
import com.flylee.gulimall.product.vo.Catalog2VO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    private final StringRedisTemplate stringRedisTemplate;

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

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return list(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getParentCid, 0));
    }

    @Override
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

        // 1、加入缓存逻辑，缓存中存的数据是JSON字符串
        // JSON跨语言、跨平台兼容
        String catalogJSONStr = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSONStr)) {
            // 2、缓存中没有，查询数据库
            System.out.println("缓存未命中...查询数据库...");
            Map<String, List<Catalog2VO>> catalogJSONFromDB = getCatalogJSONFromDBWithRedisLock();
            // 3、查到的数据再放入缓存，将对象转为JSON字符串放入缓存
            stringRedisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(catalogJSONFromDB), 1, TimeUnit.DAYS);
            return catalogJSONFromDB;
        }

        System.out.println("缓存命中...直接返回...");
        return JSON.parseObject(catalogJSONStr, new TypeReference<Map<String, List<Catalog2VO>>>(){});
    }

    private Map<String, List<Catalog2VO>> getCatalogJSONFromDBWithRedisLock() {
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "111", 300, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lock)) {
            System.out.println("加锁成功....");
            // 加锁成功... 执行业务
            Map<String, List<Catalog2VO>> dataFromDB = getDataFromDB();
            stringRedisTemplate.delete("lock");
            return dataFromDB;
        } else {
            System.out.println("加锁失败...");
            // 加锁失败...重试
            // 休眠100ms重试，自旋
            return getCatalogJSONFromDBWithLocalLock();
        }
    }

    private Map<String, List<Catalog2VO>> getDataFromDB() {
        // 得到锁之后，再去缓存确认一次，如果没有才需要继续查询
        String catalogJSONStr = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSONStr)) {
            // 缓存不为null直接返回
            return JSON.parseObject(catalogJSONStr, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        }

        System.out.println("查询了数据库......");

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