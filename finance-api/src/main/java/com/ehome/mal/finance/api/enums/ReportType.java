package com.ehome.mal.finance.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
    PROFIT_LOSS("profit_loss", "利润表"),
    BALANCE_SHEET("balance_sheet", "资产负债表"),
    CASH_FLOW("cash_flow", "现金流量表"),
    EQUITY_CHANGE("equity_change", "所有者权益变动表");

    private final String code;
    private final String desc;
}