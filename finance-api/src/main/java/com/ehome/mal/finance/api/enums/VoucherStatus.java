package com.ehome.mal.finance.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VoucherStatus {
    DRAFT(0, "草稿"),
    AUDITED(1, "已审核"),
    POSTED(2, "已过账");

    private final int code;
    private final String desc;

    public static VoucherStatus fromCode(int code) {
        for (VoucherStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown VoucherStatus code: " + code);
    }
}