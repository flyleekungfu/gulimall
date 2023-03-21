package com.flylee.gulimall.ware.feign;

import com.flylee.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     * 信息
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
