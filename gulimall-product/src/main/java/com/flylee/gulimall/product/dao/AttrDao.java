package com.flylee.gulimall.product.dao;

import com.flylee.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flylee.gulimall.product.vo.AttrGroupRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    void deleteBatchRelation(@Param("vos") AttrGroupRelationVo[] vos);

    List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
