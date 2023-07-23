package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.dao.CategoryDao;
import com.flylee.gulimall.product.entity.CategoryEntity;
import com.flylee.gulimall.product.service.CategoryService;
import com.flylee.gulimall.product.vo.Catalog2VO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        return categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0L)
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, categoryEntities)))
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort()))).collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> ids) {
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public Long[] findCategoryPathById(Long categoryId) {
        List<Long> categoryPaths = new ArrayList<>();
        findPath(categoryId, categoryPaths);
        Collections.reverse(categoryPaths);
        return categoryPaths.toArray(new Long[categoryPaths.size()]);
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return list(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getParentCid, 0));
    }

    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        // 1、查询所有一级分类
        List<CategoryEntity> level1Categories = getLevel1Categories();

        // 2、封装数据
        return level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 查询二级分类
            List<CategoryEntity> level2Categories = list(Wrappers.<CategoryEntity>lambdaQuery()
                    .eq(CategoryEntity::getParentCid, v.getCatId()));

            return level2Categories.stream().map(category2 -> {
                // 查询三级分类
                List<CategoryEntity> level3Categories = list(Wrappers.<CategoryEntity>lambdaQuery()
                        .eq(CategoryEntity::getParentCid, category2));
                List<Catalog2VO.Catalog3VO> catalog3VOList = level3Categories.stream()
                        .map(category3 -> new Catalog2VO.Catalog3VO(category2.getCatId().toString(), category3.getCatId().toString(), category3.getName()))
                        .collect(Collectors.toList());

                return new Catalog2VO(v.getCatId().toString(), catalog3VOList, category2.getCatId().toString(), category2.getName());
            }).collect(Collectors.toList());
        }));
    }

    private void findPath(Long categoryId, List<Long> categoryPaths) {
        if (categoryId != 0) {
            categoryPaths.add(categoryId);
            CategoryEntity byId = getById(categoryId);
            findPath(byId.getParentCid(), categoryPaths);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> categoryEntities) {
        return categoryEntities.stream().filter(c -> c.getParentCid().equals(categoryEntity.getCatId()))
                .peek(eachCategoryEntity -> eachCategoryEntity.setChildren(getChildren(eachCategoryEntity, categoryEntities)))
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort()))).collect(Collectors.toList());
    }

}