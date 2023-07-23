package com.flylee.gulimall.product.dao;

import com.flylee.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId, @Param("publishStatus") int publishStatus);
}
