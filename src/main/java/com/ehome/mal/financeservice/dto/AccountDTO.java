package com.ehome.mal.financeservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    @NotBlank(message = "科目编码不能为空")
    private String accountCode;

    @NotBlank(message = "科目名称不能为空")
    private String accountName;

    @NotBlank(message = "科目类型不能为空")
    private String accountType;

    private Long parentId;

    @NotNull(message = "科目级次不能为空")
    private Integer level;

    @NotBlank(message = "余额方向不能为空")
    private String direction;

    private BigDecimal initialDebit;

    private BigDecimal initialCredit;

    private Integer auxiliaryFlag;

    private String remark;
}
