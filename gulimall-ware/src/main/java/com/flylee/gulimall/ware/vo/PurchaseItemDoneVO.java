package com.flylee.gulimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVO {

    private Long itemId;
    private Integer status;
    private String reason;
}
