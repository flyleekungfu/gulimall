package com.flylee.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 19:49:23
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

