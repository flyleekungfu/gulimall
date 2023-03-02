package com.flylee.gulimall.common.param;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class BasePageParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private Integer page;
    private Integer limit;
    private String key;
    /**
     * 排序字段
     */
    private String sidx;
    /**
     * 排序方式
     */
    private String order;
}
