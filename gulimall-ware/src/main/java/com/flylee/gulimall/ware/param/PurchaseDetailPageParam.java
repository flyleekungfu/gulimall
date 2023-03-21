package com.flylee.gulimall.ware.param;

import com.flylee.gulimall.common.param.BasePageParam;
import lombok.Data;

@Data
public class PurchaseDetailPageParam extends BasePageParam {

    private Integer status;

    private Long wareId;
}
