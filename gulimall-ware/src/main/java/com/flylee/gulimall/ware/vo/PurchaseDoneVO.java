package com.flylee.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVO {

    /**
     * 采购单id
     */
    @NotNull
    private Long id;

    @NotEmpty
    private List<PurchaseItemDoneVO> items;

}
