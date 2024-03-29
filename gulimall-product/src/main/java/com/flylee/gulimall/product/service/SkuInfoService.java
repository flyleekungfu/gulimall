package com.flylee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.product.entity.SkuInfoEntity;
import com.flylee.gulimall.product.param.SkuInfoListParam;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(SkuInfoListParam param);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);
}

