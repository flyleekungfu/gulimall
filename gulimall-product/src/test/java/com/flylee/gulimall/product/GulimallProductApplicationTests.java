package com.flylee.gulimall.product;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flylee.gulimall.product.entity.BrandEntity;
import com.flylee.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Resource
    private BrandService brandService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void contextLoads() {
        //BrandEntity brandEntity = new BrandEntity();
        //brandEntity.setName("测试");
        //brandService.save(brandEntity);

        BrandEntity brandEntity = brandService.getOne(Wrappers.<BrandEntity>lambdaQuery()
                .eq(BrandEntity::getName, "测试"));
        System.out.println(brandEntity);
    }

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("test", "test123");
        String test = ops.get("test");
        System.out.println(test);
    }

}
