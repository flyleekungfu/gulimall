package com.flylee.gulimall.product.feign;

import com.flylee.gulimall.common.to.es.SkuEsModel;
import com.flylee.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "gulimall-search")
public interface SearchFeignService {

    /**
     * 上架商品
     *
     * @param skuEsModels skuEsModels
     * @return 结果
     */
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
