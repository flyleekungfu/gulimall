package com.flylee.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrGroupRelationVo {

    private Long attrId;

    private Long attrGroupId;

    public Long getAttrId() {
        return attrId;
    }

    public void setAttrId(Long attrId) {
        this.attrId = attrId;
    }

    public Long getAttrGroupId() {
        return attrGroupId;
    }

    public void setAttrGroupId(Long attrGroupId) {
        this.attrGroupId = attrGroupId;
    }
}
