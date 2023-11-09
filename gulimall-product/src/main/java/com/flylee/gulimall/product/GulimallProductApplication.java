package com.flylee.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 7、整合Redisson作为分布式锁等功能框架
 *     1）、引入依赖
 *         <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.5</version>
 *         </dependency>
 *     2）、配置Redisson
 *         MyRedissonConfig给容器中配置一个RedissonClient实例即可
 *     3）、使用
 *         参照文档做：<a href="https://github.com/redisson/redisson/wiki/8.-distributed-locks-and-synchronizers">...</a>
 *
 * 8、整合Spring Cache简化缓存开发
 *      1）、引入依赖
 *          spring-boot-starter-cache、spring-boot-starter-data-redis
 *      2）、写配置
 *          （1）、自动配置了哪些
 *              CacheAutoConfiguration会导入RedisCacheConfiguration
 *              自动配置好了缓存管理器RedisCacheManager
 *          （2）、配置使用Redis作为缓存
 *              spring.cache.type=redis
*       3）、测试使用缓存
 *          @Cacheable: Triggers cache population.：触发将数据保存到缓存的操作
 *          @CacheEvict: Triggers cache eviction.：触发将数据从缓存删除的操作
 *          @CachePut: Updates the cache without interfering with the method execution.：不影响方法执行更新缓存
 *          @Caching: Regroups multiple cache operations to be applied on a method.：组合以上多个操作
 *          @CacheConfig: Shares some common cache-related settings at class-level.：在类级别共享缓存的相同配置
 *          1）、开启缓存功能 @EnableCaching
 *          2）、只需要使用注解就能完成缓存操作
 *     4）、原理
 *          CacheAutoConfiguration -> RedisCacheConfiguration -> 自动配置了RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定使用什么配置
 *          ->如果RedisCacheConfiguration有就用已有的，没有就用默认配置
 *          ->想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *          ->就会应用到当前RedisCacheManager管理的所有缓存中
 *
 *
 *
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableFeignClients(basePackages = "com.flylee.gulimall.product.feign")
@MapperScan("com.flylee.gulimall.product.dao")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
