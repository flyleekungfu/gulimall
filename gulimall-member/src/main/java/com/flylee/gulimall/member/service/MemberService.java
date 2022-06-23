package com.flylee.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:59:40
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

