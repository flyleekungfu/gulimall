package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.product.constant.ProductConstant;
import com.flylee.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.flylee.gulimall.product.dao.AttrDao;
import com.flylee.gulimall.product.dao.AttrGroupDao;
import com.flylee.gulimall.product.dao.CategoryDao;
import com.flylee.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.flylee.gulimall.product.entity.AttrEntity;
import com.flylee.gulimall.product.entity.AttrGroupEntity;
import com.flylee.gulimall.product.entity.CategoryEntity;
import com.flylee.gulimall.product.service.AttrService;
import com.flylee.gulimall.product.service.CategoryService;
import com.flylee.gulimall.product.vo.AttrRespVo;
import com.flylee.gulimall.product.vo.AttrVo;
import com.flylee.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private CategoryDao categoryDao;
    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private CategoryService categoryService;
    @Resource
    private AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, String attrType, long catelogId) {
        // 根据attrType进行查询，1规格参数，2销售属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        //搜索的模糊查询
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> wrapper.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> respVos = records.stream().map(attrEntity -> {
            AttrRespVo respVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, respVo);

            // 查询分类并设置分类名
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            respVo.setCatelogName(categoryEntity.getName());

            if ("base".equalsIgnoreCase(attrType)) {
                // 查询参数、分组关系
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                // 如果分组id不为空。则查出分组名
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrAttrgroupRelationEntity.getAttrGroupId()));
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            return respVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
        // 如果分组id不为空，说明是规格参数而不是销售属性，则对属性-分组表进行更新
        if (attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        // 只有当属性分组不为空时，说明更新的是规则参数，则需要更新关联表
        if (attr.getAttrGroupId() != null) {
            // 查询属性-分组名对应关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrAttrgroupRelationEntity.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.baseMapper.selectById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if (attrAttrgroupRelationEntity != null) {
            Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
            attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
        }

        Long catelogId = attrEntity.getCatelogId();
        if (catelogId != null) {
            CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
            attrRespVo.setCatelogName(categoryEntity.getName());
            attrRespVo.setCatelogPath(categoryService.findCategoryPathById(catelogId));
        }

        return attrRespVo;
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        if (CollectionUtils.isEmpty(attrAttrgroupRelationEntities)) {
            return new ArrayList<>(0);
        }

        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        return this.baseMapper.selectBatchIds(attrIds);
    }

    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 查询分组同目录下的属性，过滤掉销售属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        //模糊搜索条件
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((a) -> a.like("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        List<AttrEntity> records = page.getRecords();
        // 过滤掉已经关联的属性
        List<AttrEntity> attrEntities = records.stream().filter(attrEntity -> {
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (count > 0) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());

        page.setRecords(attrEntities);
        return new PageUtils(page);
    }

    @Override
    public void saveRelationBatch(List<AttrAttrgroupRelationEntity> relationEntities) {
        relationEntities.forEach(entity -> attrAttrgroupRelationDao.insert(entity));
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        attrDao.deleteBatchRelation(vos);
    }

}