package com.flylee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.constant.WareConstant;
import com.flylee.gulimall.common.param.BasePageParam;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.ware.dao.PurchaseDao;
import com.flylee.gulimall.ware.entity.PurchaseDetailEntity;
import com.flylee.gulimall.ware.entity.PurchaseEntity;
import com.flylee.gulimall.ware.service.PurchaseDetailService;
import com.flylee.gulimall.ware.service.PurchaseService;
import com.flylee.gulimall.ware.service.WareSkuService;
import com.flylee.gulimall.ware.vo.MergeVO;
import com.flylee.gulimall.ware.vo.PurchaseDoneVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;
    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(BasePageParam pageParam) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = Wrappers.<PurchaseEntity>lambdaQuery();
        // 只查询新建和已分配状态
        queryWrapper.in(PurchaseEntity::getStatus, Arrays.asList(WareConstant.PurchaseStatusEnum.CREATED.getCode(), WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()));
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(pageParam), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId != null) {
            PurchaseEntity purchaseEntity = getById(purchaseId);
            // 如果不是新建和已分配状态则返回
            if (purchaseEntity.getStatus() != WareConstant.PurchaseStatusEnum.CREATED.getCode() && purchaseEntity.getStatus() != WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return;
            }
        } else {
            // 如果采购单id不存在，新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode())
                    .setCreateTime(new Date())
                    .setUpdateTime(new Date());
            save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = mergeVO.getItems().stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item)
                    .setPurchaseId(finalPurchaseId)
                    .setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId)
                .setUpdateTime(new Date());
        updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 只查询新建或已分配的采购单
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = Wrappers.<PurchaseEntity>lambdaQuery()
                .in(PurchaseEntity::getId, ids)
                .in(PurchaseEntity::getStatus, Arrays.asList(WareConstant.PurchaseStatusEnum.CREATED.getCode(), WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()));
        List<PurchaseEntity> purchaseEntities = list(queryWrapper);
        if (CollectionUtils.isEmpty(purchaseEntities)) {
            return;
        }

        // 修改采购单状态
        List<PurchaseEntity> purchaseEntitiesForUpdate = purchaseEntities.stream().map(purchaseEntity -> {
            PurchaseEntity purchaseEntityForUpdate = new PurchaseEntity();
            purchaseEntityForUpdate.setId(purchaseEntity.getId())
                    .setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode())
                    .setUpdateTime(new Date());
            return purchaseEntityForUpdate;
        }).collect(Collectors.toList());
        updateBatchById(purchaseEntitiesForUpdate);

        // 修改采购项状态
        List<Long> purchaseIds = purchaseEntities.stream().map(PurchaseEntity::getId).collect(Collectors.toList());
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByPurchaseIds(purchaseIds);
        List<PurchaseDetailEntity> purchaseDetailEntitiesForUpdate = purchaseDetailEntities.stream().map(purchaseDetailEntity -> {
            PurchaseDetailEntity purchaseDetailEntityForUpdate = new PurchaseDetailEntity();
            purchaseDetailEntityForUpdate.setId(purchaseDetailEntity.getId())
                    .setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
            return purchaseDetailEntityForUpdate;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntitiesForUpdate);
    }

    @Override
    public void done(PurchaseDoneVO purchaseDoneVO) {
        // 采购项是否全部成功
        AtomicBoolean flagAtomic = new AtomicBoolean(true);

        // 处理采购项状态
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDoneVO.getItems().stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item.getItemId());
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASEERROR.getCode()) {
                purchaseDetailEntity.setStatus(item.getStatus());
                flagAtomic.set(false);
            } else {
                // 采购成功
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());

                // 将采购成功的商品入库
                PurchaseDetailEntity purchaseDetailEntityById = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(purchaseDetailEntityById.getSkuId(), purchaseDetailEntityById.getWareId(), purchaseDetailEntityById.getSkuNum());
            }

            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        // 处理采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVO.getId())
                .setStatus(flagAtomic.get() ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASEERROR.getCode())
                .setUpdateTime(new Date());
        updateById(purchaseEntity);
    }

}