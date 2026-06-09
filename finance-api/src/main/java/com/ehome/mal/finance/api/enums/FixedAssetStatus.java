package com.ehome.mal.finance.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FixedAssetStatus {
    IN_SERVICE(1, "在役"),
    DISPOSED(2, "已处置");

    private final int code;
    private final String desc;
}