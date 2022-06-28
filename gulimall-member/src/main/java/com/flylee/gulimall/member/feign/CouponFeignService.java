package com.flylee.gulimall.member.feign;

import com.flylee.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 优惠券Feign服务
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    /**
     * 测试OpenFeign
     *
     * @return 会员优惠券列表
     */
    @RequestMapping("coupon/coupon/member/list")
    R memberCoupons();
}
