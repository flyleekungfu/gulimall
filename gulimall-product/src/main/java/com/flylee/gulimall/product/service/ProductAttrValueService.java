package com.flylee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);
}

