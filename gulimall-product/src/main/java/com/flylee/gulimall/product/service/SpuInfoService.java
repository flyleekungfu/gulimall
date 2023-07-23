package com.flylee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flylee.gulimall.common.utils.PageUtils;
import com.flylee.gulimall.product.entity.SpuInfoEntity;
import com.flylee.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author flylee
 * @email flyleekungfu@163.com
 * @date 2022-06-23 07:53:32
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuSaveVo(SpuSaveVo spuSaveInfo);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上级
     * @param spuId spu主键
     */
    void up(Long spuId);
}

