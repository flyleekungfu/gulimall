package com.flylee.gulimall.coupon;

import com.flylee.gulimall.coupon.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Resource
    private CouponService couponService;

    @Test
    void contextLoads() {
        System.out.println(couponService.list().size());
    }

}
