package com.flylee.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.param.BasePageParam;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.ware.entity.PurchaseEntity;
import com.flylee.gulimall.ware.vo.MergeVO;
import com.flylee.gulimall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:49:23
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceived(BasePageParam pageParam);

    void mergePurchase(MergeVO mergeVO);

    void received(List<Long> ids);

    void done(PurchaseDoneVO purchaseDoneVO);
}

