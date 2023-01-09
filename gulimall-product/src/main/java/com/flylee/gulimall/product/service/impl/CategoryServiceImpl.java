package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.dao.CategoryDao;
import com.flylee.gulimall.product.entity.CategoryEntity;
import com.flylee.gulimall.product.service.CategoryService;
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