package com.flylee.gulimall.product.dao;

import com.flylee.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
