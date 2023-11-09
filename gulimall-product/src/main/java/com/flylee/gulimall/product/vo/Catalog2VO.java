package com.flylee.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog2VO implements Serializable {

    private static final long serialVersionUID = 1L;

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
    public static class Catalog3VO implements Serializable{

        private static final long serialVersionUID = 1L;

        /**
         * 父分类，2级分类id
         */
        private String catalog2Id;

        private String id;

        private String name;
    }
}
