package com.flylee.gulimall.order;

import com.flylee.gulimall.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GulimallOrderApplicationTests {

    @Resource
    private OrderService orderService;

    @Test
    void contextLoads() {
        System.out.println(orderService.list().size());
    }

}
