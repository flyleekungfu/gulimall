package com.flylee.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog2VO {
    /**
     * 1级父分类id
     */
    private String catalog1Id;

    /**
     * 三级子分类
     */
    private List<Catalog3VO> catalog3List;

    private String id;

    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catalog3VO {

        /**
         * 父分类，2级分类id
         */
        private String catalog2Id;

        private String id;

        private String name;
    }
}
