package com.flylee.gulimall.product.feign;

import com.flylee.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "gulimall-ware")
public interface WareFeignService {

    /**
     * sku是否有库存
     *
     * @param skuIds sku主键列表
     * @return sku是否有库存
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}
