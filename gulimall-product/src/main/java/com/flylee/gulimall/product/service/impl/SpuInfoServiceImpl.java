package com.flylee.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.to.SkuHasStockVO;
import com.flylee.gulimall.common.to.SkuReductionTo;
import com.flylee.gulimall.common.to.SpuBoundTo;
import com.flylee.gulimall.common.to.es.SkuEsModel;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.common.utils.R;
import com.flylee.gulimall.product.constant.ProductConstant;
import com.flylee.gulimall.product.dao.SpuInfoDao;
import com.flylee.gulimall.product.entity.*;
import com.flylee.gulimall.product.feign.CouponFeignService;
import com.flylee.gulimall.product.feign.SearchFeignService;
import com.flylee.gulimall.product.feign.WareFeignService;
import com.flylee.gulimall.product.service.*;
import com.flylee.gulimall.product.vo.Attr;
import com.flylee.gulimall.product.vo.BaseAttrs;
import com.flylee.gulimall.product.vo.Skus;
import com.flylee.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private SearchFeignService searchFeignService;

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
                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().filter(i -> !StringUtils.isEmpty(i.getImgUrl())).map(image -> {
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
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(q -> q.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key));
        }

        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq(SpuInfoEntity::getPublishStatus, Integer.parseInt(status));
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equals(brandId)) {
            queryWrapper.eq(SpuInfoEntity::getBrandId, Long.parseLong(brandId));
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq(SpuInfoEntity::getCatalogId, Long.parseLong(catelogId));
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1、查出当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // 4、查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> productAttrValues = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = productAttrValues.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        List<SkuEsModel.Attrs> attrsList = productAttrValues.stream().filter(productAttrValue -> searchAttrIds.contains(productAttrValue.getAttrId())).map(productAttrValue -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(productAttrValue, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // 1、发送远程调用，库存系统查询是否有库存
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkuHasStock(skuIds);
            TypeReference<List<SkuHasStockVO>> typeReference = new TypeReference<List<SkuHasStockVO>>(){};
            List<SkuHasStockVO> skuHasStockVOList = r.getData(typeReference);
            stockMap = skuHasStockVOList.stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因：{}", e);
        }

        // 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);

            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            // 1、发送远程调用，库存系统查询是否有库存
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // 2、热度评分。0
            skuEsModel.setHotScore(0L);

            // 3、查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            // 设置检索属性
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        // 5、将数据发给es进行保存：gulimall-search
        R r = searchFeignService.productStatusUp(skuEsModels);
        if (r.getCode() == 0) {
            // 远程调用成功
            // 6、修改当前spu状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {

        }
    }

}