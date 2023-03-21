package com.flylee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.ware.WareSkuPageParam;
import com.flylee.gulimall.ware.dao.WareSkuDao;
import com.flylee.gulimall.ware.entity.WareSkuEntity;
import com.flylee.gulimall.ware.feign.ProductFeignService;
import com.flylee.gulimall.ware.service.WareSkuService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;
    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryByCondition(WareSkuPageParam pageParam) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = Wrappers.<WareSkuEntity>lambdaQuery()
                .eq(pageParam.getSkuId() != null, WareSkuEntity::getSkuId, pageParam.getSkuId())
                .eq(pageParam.getWareId() != null, WareSkuEntity::getWareId, pageParam.getWareId());

        String key = pageParam.getKey();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> w.like(WareSkuEntity::getId, key).or().like(WareSkuEntity::getSkuName, key));
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(pageParam),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 根据skuId和wareId查询库存，没有则新增，有则更新
        WareSkuEntity wareSkuEntity = getOne(Wrappers.<WareSkuEntity>lambdaQuery()
                .eq(WareSkuEntity::getSkuId, skuId)
                .eq(WareSkuEntity::getWareId, wareId)
                .last("limit 1"));
        if (wareSkuEntity == null) {
            // 没有库存则新增
            wareSkuEntity.setSkuId(skuId)
                    .setWareId(wareId)
                    .setStock(skuNum);
            // 设置skuName,//远程查询SKU的name，若失败无需回滚
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                log.error("查询sku信息异常", e);
            }
            save(wareSkuEntity);
        } else {
            // 已有库存则更新
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

}