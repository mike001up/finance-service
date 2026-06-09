package com.ehome.mal.finance.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String accountCode;

    private String accountName;

    private String accountType;

    private Integer level;

    private String direction;

    private BigDecimal initialDebit;

    private BigDecimal initialCredit;

    private List<AccountTreeVO> children;
}
