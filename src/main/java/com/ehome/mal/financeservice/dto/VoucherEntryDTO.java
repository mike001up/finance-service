package com.ehome.mal.financeservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class VoucherEntryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "摘要不能为空")
    private String summary;

    @NotNull(message = "科目ID不能为空")
    private Long accountId;

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
