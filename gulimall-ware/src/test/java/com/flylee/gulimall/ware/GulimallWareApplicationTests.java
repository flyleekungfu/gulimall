package com.flylee.gulimall.ware;

import com.flylee.gulimall.ware.service.WareInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GulimallWareApplicationTests {

    @Resource
    private WareInfoService wareInfoService;

    @Test
    void contextLoads() {
        System.out.println(wareInfoService.list().size());
    }

}
