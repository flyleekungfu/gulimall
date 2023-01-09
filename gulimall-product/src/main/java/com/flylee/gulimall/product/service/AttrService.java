package com.flylee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.flylee.gulimall.product.entity.AttrEntity;
import com.flylee.gulimall.product.vo.AttrRespVo;
import com.flylee.gulimall.product.vo.AttrVo;
import com.flylee.gulimall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, String attrType, long catelogId);

    void saveAttr(AttrVo attr);

    void updateArr(AttrVo attr);

    AttrRespVo getAttrInfo(Long attrId);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params);

    void saveRelationBatch(List<AttrAttrgroupRelationEntity> relationEntities);

    void deleteRelation(AttrGroupRelationVo[] vos);
}

