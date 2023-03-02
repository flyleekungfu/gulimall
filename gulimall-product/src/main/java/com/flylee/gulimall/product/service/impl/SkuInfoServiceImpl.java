package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.dao.SkuInfoDao;
import com.flylee.gulimall.product.entity.SkuInfoEntity;
import com.flylee.gulimall.product.param.SkuInfoListParam;
import com.flylee.gulimall.product.service.SkuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(SkuInfoListParam param) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = Wrappers.lambdaQuery();

        String key = param.getKey();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> w.like(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key));
        }

        Long catelogId = param.getCatelogId();
        if (catelogId != null && catelogId != 0L) {
            queryWrapper.eq(SkuInfoEntity::getCatalogId, catelogId);
        }

        Long brandId = param.getBrandId();
        if (brandId != null  && brandId != 0L) {
            queryWrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }

        BigDecimal min = param.getMin();
        if (min != null) {
            queryWrapper.ge(SkuInfoEntity::getPrice, min);
        }

        BigDecimal max = param.getMax();
        if (max != null) {
            queryWrapper.le(SkuInfoEntity::getPrice, max);
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(param),
                queryWrapper
        );

        return new PageUtils(page);
    }
}