package com.ehome.mal.finance.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class FixedAssetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    @NotBlank(message = "资产编码不能为空")
    private String assetCode;

    @NotBlank(message = "资产名称不能为空")
    private String assetName;

    @NotBlank(message = "资产类别不能为空")
    private String assetCategory;

    @NotNull(message = "借方科目不能为空")
    private Long accountDebitId;

    @NotNull(message = "贷方科目不能为空")
    private Long accountCreditId;

    @NotNull(message = "原值不能为空")
    private BigDecimal originalValue;

    private BigDecimal salvageValue;

    @NotNull(message = "使用年限不能为空")
    private Integer usefulLife;

    @NotBlank(message = "折旧方法不能为空")
    private String depreciationMethod;

    @NotNull(message = "取得日期不能为空")
    private Instant acquisitionDate;

    private Instant depreciationStartDate;

    private String department;

    private String custodian;

    private String location;

    private String remark;
}
