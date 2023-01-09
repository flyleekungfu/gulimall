package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.dao.AttrGroupDao;
import com.flylee.gulimall.product.entity.AttrEntity;
import com.flylee.gulimall.product.entity.AttrGroupEntity;
import com.flylee.gulimall.product.service.AttrGroupService;
import com.flylee.gulimall.product.service.AttrService;
import com.flylee.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catelogId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrVo> vos = attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrVo vo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(attrGroupEntity, vo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupEntity.getAttrGroupId());
            vo.setAttrs(attrs);
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

}