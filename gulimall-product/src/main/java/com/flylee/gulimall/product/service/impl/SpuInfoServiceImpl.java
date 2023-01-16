package com.flylee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.to.SkuReductionTo;
import com.flylee.gulimall.common.to.SpuBoundTo;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.product.dao.SpuInfoDao;
import com.flylee.gulimall.product.entity.*;
import com.flylee.gulimall.product.feign.CouponFeignService;
import com.flylee.gulimall.product.service.*;
import com.flylee.gulimall.product.vo.Attr;
import com.flylee.gulimall.product.vo.BaseAttrs;
import com.flylee.gulimall.product.vo.Skus;
import com.flylee.gulimall.product.vo.SpuSaveVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuSaveVo(SpuSaveVo spuSaveVo) {
        // 1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        baseMapper.insert(spuInfoEntity);

        // 2、保存Spu的描述 pms_spu_info_desc
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        Long spuId = spuInfoEntity.getId();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(String.join(",", spuSaveVo.getDecript()));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        // 3、保存spu的图片集 pms_spu_images
        spuImagesService.saveImages(spuId, spuSaveVo.getImages());

        // 4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<Long> attrIds = baseAttrs.stream().map(BaseAttrs::getAttrId).collect(Collectors.toList());
        Collection<AttrEntity> attrEntities = attrService.listByIds(attrIds);
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity productAttrValueEntity = ProductAttrValueEntity.builder()
                    .spuId(spuId).attrId(baseAttr.getAttrId()).attrValue(baseAttr.getAttrValues()).quickShow(baseAttr.getShowDesc()).build();
            attrEntities.stream().filter(a -> Objects.equals(baseAttr.getAttrId(), a.getAttrId())).findAny().ifPresent(attrEntity -> productAttrValueEntity.setAttrName(attrEntity.getAttrName()));
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);

        // 5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(spuSaveVo.getBounds(), spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //5、保存当前spu对应的所有sku信息；
        List<Skus> skus = spuSaveVo.getSkus();
        if (CollectionUtils.isNotEmpty(skus)) {
            skus.forEach(sku -> {
                // 5.1）、sku的基本信息；pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuId)
                        .setCatalogId(spuInfoEntity.getCatalogId())
                        .setBrandId(spuInfoEntity.getBrandId())
                        .setSaleCount(0L);
                sku.getImages().stream().filter(i -> i.getDefaultImg() == 1).findAny().ifPresent(defaultImg -> {
                    skuInfoEntity.setSkuDefaultImg(defaultImg.getImgUrl());
                });
                skuInfoService.save(skuInfoEntity);

                // 5.2）、sku的图片信息；pms_sku_image
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().filter(i -> StringUtils.isNotEmpty(i.getImgUrl())).map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(image, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                // 5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo= new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                    R r1 = couponFeignService.saveSkuReductionTo(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = Wrappers.lambdaQuery();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(q -> q.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key));
        }


        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

}