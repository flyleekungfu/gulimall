package com.flylee.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:36:03
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

