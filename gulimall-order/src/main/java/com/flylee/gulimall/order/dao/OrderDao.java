package com.flylee.gulimall.order.dao;

import com.flylee.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:36:03
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
