package com.flylee.gulimall.coupon.dao;

import com.flylee.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:30:58
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
