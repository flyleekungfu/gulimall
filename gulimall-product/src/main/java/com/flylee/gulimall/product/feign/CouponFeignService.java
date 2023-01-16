package com.flylee.gulimall.product.feign;

import com.flylee.gulimall.common.to.SkuReductionTo;
import com.flylee.gulimall.common.to.SpuBoundTo;
import com.flylee.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 保存
     */
    @RequestMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @RequestMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReductionTo(@RequestBody SkuReductionTo skuReductionTo);
}
