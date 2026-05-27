package com.ehome.mal.financeservice.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TrialBalanceVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountCode;

    private String accountName;

    private BigDecimal initialDebit;

    private BigDecimal initialCredit;

    private BigDecimal periodDebit;

    private BigDecimal periodCredit;

    private BigDecimal yearDebit;

    private BigDecimal yearCredit;

    private BigDecimal endingBalance;
}
