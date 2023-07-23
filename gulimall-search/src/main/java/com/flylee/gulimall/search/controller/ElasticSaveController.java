package com.flylee.gulimall.search.controller;

import com.flylee.gulimall.common.exception.BizCodeEnum;
import com.flylee.gulimall.common.to.es.SkuEsModel;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Resource
    private ProductSaveService productSaveService;

    /**
     * 上架商品
     * @param skuEsModels skuEsModels
     * @return 结果
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean isSuccess;
        try {
            isSuccess = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误：{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (isSuccess) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }


}
