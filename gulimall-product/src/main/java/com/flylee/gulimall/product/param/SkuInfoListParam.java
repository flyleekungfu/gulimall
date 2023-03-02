package com.flylee.gulimall.product.param;

import com.flylee.gulimall.common.param.BasePageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 查询sku信息列表参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SkuInfoListParam extends BasePageParam {
    private Long catelogId;
    private Long brandId;
    private BigDecimal min;
    private BigDecimal max;
}
