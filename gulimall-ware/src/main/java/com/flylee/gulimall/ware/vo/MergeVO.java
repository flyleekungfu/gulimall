package com.flylee.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVO {

    /**
     * 采购单id
     */
    private Long purchaseId;

    /**
     * 合并项id集合
     */
    private List<Long> items;
}
