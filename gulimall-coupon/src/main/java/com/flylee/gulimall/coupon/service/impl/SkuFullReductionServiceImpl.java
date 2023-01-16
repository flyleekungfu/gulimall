package com.flylee.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flylee.gulimall.common.to.SkuReductionTo;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.common.utils.Query;
import com.flylee.gulimall.coupon.dao.SkuFullReductionDao;
import com.flylee.gulimall.coupon.entity.MemberPriceEntity;
import com.flylee.gulimall.coupon.entity.SkuFullReductionEntity;
import com.flylee.gulimall.coupon.entity.SkuLadderEntity;
import com.flylee.gulimall.coupon.service.MemberPriceService;
import com.flylee.gulimall.coupon.service.SkuFullReductionService;
import com.flylee.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderService skuLadderService;
    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReductionTo(SkuReductionTo skuReductionTo) {
        // sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId())
        	.setFullCount(skuReductionTo.getFullCount())
        	.setDiscount(skuReductionTo.getDiscount())
        	.setAddOther(skuReductionTo.getCountStatus());
        BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
        skuLadderService.save(skuLadderEntity);

        // sms_sku_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        this.save(skuFullReductionEntity);

        // member_price
        List<MemberPriceEntity> memberPriceEntities = skuReductionTo.getMemberPrice().stream().filter(mp -> mp.getPrice().compareTo(BigDecimal.ZERO) != 0).map(memberPrice -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId())
                    .setMemberLevelId(memberPrice.getId())
                    .setMemberLevelName(memberPrice.getName())
                    .setMemberPrice(memberPrice.getPrice());
            return memberPriceEntity;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);

    }

}