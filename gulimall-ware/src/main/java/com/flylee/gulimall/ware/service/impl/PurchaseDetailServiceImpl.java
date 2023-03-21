package com.flylee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.ware.dao.PurchaseDetailDao;
import com.flylee.gulimall.ware.entity.PurchaseDetailEntity;
import com.flylee.gulimall.ware.param.PurchaseDetailPageParam;
import com.flylee.gulimall.ware.service.PurchaseDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryByCondition(PurchaseDetailPageParam pageParam) {
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = Wrappers.<PurchaseDetailEntity>lambdaQuery()
                .eq(pageParam.getStatus() != null, PurchaseDetailEntity::getStatus, pageParam.getStatus())
                .eq(pageParam.getWareId() != null, PurchaseDetailEntity::getWareId, pageParam.getWareId());
        if (!StringUtils.isEmpty(pageParam.getKey())) {
            queryWrapper.and(a -> a.like(PurchaseDetailEntity::getPurchaseId, pageParam.getKey()).or().like(PurchaseDetailEntity::getSkuId, pageParam.getKey()));
        }
        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(pageParam), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listByPurchaseIds(List<Long> purchaseIds) {
        if (CollectionUtils.isEmpty(purchaseIds)) {
            return new ArrayList<>(0);
        }

        return list(Wrappers.<PurchaseDetailEntity>lambdaQuery()
                .in(PurchaseDetailEntity::getPurchaseId, purchaseIds));
    }

}