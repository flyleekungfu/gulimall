package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.dao.BrandDao;
import com.flylee.gulimall.product.dao.CategoryBrandRelationDao;
import com.flylee.gulimall.product.dao.CategoryDao;
import com.flylee.gulimall.product.entity.BrandEntity;
import com.flylee.gulimall.product.entity.CategoryBrandRelationEntity;
import com.flylee.gulimall.product.entity.CategoryEntity;
import com.flylee.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private BrandDao brandDao;
    @Resource
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        BrandEntity brandEntity = brandDao.selectById(brandId);
        categoryBrandRelation.setBrandName(brandEntity.getName());

        Long catelogId = categoryBrandRelation.getCatelogId();
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public List<CategoryBrandRelationEntity> getBrandsByCayId(Long catelogId) {
        return baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catelogId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        update(Wrappers.<CategoryBrandRelationEntity>lambdaUpdate()
                .set(CategoryBrandRelationEntity::getCatelogName, name)
                .eq(CategoryBrandRelationEntity::getCatelogId, catId));
    }

}