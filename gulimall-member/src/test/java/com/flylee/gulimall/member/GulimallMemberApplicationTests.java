package com.flylee.gulimall.member;

import com.flylee.gulimall.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GulimallMemberApplicationTests {

    @Resource
    private MemberService memberService;

    @Test
    void contextLoads() {
        System.out.println(memberService.list().size());
    }

}
