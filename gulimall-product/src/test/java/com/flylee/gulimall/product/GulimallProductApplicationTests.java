package com.flylee.gulimall.product;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flylee.gulimall.product.entity.BrandEntity;
import com.flylee.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GulimallProductApplicationTests {

    @Resource
    private BrandService brandService;

    @Test
    void contextLoads() {
        //BrandEntity brandEntity = new BrandEntity();
        //brandEntity.setName("测试");
        //brandService.save(brandEntity);

        BrandEntity brandEntity = brandService.getOne(Wrappers.<BrandEntity>lambdaQuery()
                .eq(BrandEntity::getName, "测试"));
        System.out.println(brandEntity);
    }

}
