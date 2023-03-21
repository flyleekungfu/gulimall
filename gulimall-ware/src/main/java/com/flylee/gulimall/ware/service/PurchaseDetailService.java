package com.flylee.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.ware.entity.PurchaseDetailEntity;
import com.flylee.gulimall.ware.param.PurchaseDetailPageParam;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:49:23
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryByCondition(PurchaseDetailPageParam pageParam);

    List<PurchaseDetailEntity> listByPurchaseIds(List<Long> purchaseIds);
}

