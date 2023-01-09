package com.flylee.gulimall.product.vo;


import com.flylee.gulimall.product.entity.AttrEntity;
import com.flylee.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrVo extends AttrGroupEntity {

    private List<AttrEntity> attrs;
}
