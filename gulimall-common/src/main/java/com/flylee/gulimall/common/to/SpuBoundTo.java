package com.flylee.gulimall.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分To（Transfer Object）
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
