package com.flylee.gulimall.ware;

import com.flylee.gulimall.common.param.BasePageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品库存分页参数
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WareSkuPageParam extends BasePageParam {

    /**
     * sku_id
     */
    private Long skuId;

    /**
     * 仓库id
     */
    private Long wareId;
}
