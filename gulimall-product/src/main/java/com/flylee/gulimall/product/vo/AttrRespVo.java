package com.flylee.gulimall.product.vo;

import com.flylee.gulimall.product.entity.AttrEntity;
import lombok.Data;

/**
 * 属性响应VO
 *
 */
@Data
public class AttrRespVo extends AttrEntity {

    /**
     * 所属分类
     */
    private String catelogName;

    /**
     * 所属分组
     */
    private String groupName;

    /**
     * 分类路径
     */
    private Long[] catelogPath;
}
