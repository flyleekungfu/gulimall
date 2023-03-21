package com.flylee.gulimall.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class WareConstant {

    @AllArgsConstructor
    @Getter
    public enum PurchaseStatusEnum {
        CREATED(0, "新建状态"),
        ASSIGNED(1, "已分配"),
        RECEIVED(2, "已领取"),
        FINISH(3, "已完成"),
        HASEERROR(4, "有异常");

        private final int code;
        private final String message;
    }

    @AllArgsConstructor
    @Getter
    public enum PurchaseDetailStatusEnum {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"),
        FINISH(3, "已完成"),
        HASEERROR(4, "采购失败");

        private final int code;
        private final String message;
    }
}
