package com.ehome.mal.finance.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PeriodStatus {
    NOT_OPENED(0, "未开启"),
    OPENED(1, "已开启"),
    CLOSED(2, "已关闭");

    private final int code;
    private final String desc;

    public static PeriodStatus fromCode(int code) {
        for (PeriodStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PeriodStatus code: " + code);
    }
}