package com.ehome.mal.finance.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class VoucherEntryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer entrySeq;

    private String summary;

    private Long accountId;

    private String accountCode;

    private String accountName;

    private BigDecimal debitAmount;

    private BigDecimal creditAmount;

    private String auxiliary1Type;
    private String auxiliary1Value;
    private String auxiliary2Type;
    private String auxiliary2Value;
    private String auxiliary3Type;
    private String auxiliary3Value;
    private String auxiliary4Type;
    private String auxiliary4Value;
}
