package com.ehome.mal.finance.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DepreciationMethod {
    STRAIGHT_LINE("STRAIGHT_LINE", "直线法"),
    DOUBLE_DECLINING("DOUBLE_DECLINING", "双倍余额递减法");

    private final String code;
    private final String desc;

    public static DepreciationMethod fromCode(String code) {
        for (DepreciationMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown DepreciationMethod code: " + code);
    }
}