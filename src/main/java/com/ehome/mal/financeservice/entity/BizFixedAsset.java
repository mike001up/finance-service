package com.ehome.mal.financeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_fixed_asset")
public class BizFixedAsset implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String assetCode;

    private String assetName;

    private String assetCategory;

    private Long accountDebitId;

    private Long accountCreditId;

    private BigDecimal originalValue;

    private BigDecimal salvageValue;

    private BigDecimal depreciationValue;

    private BigDecimal netValue;

    private Integer usefulLife;

    private String depreciationMethod;

    private LocalDateTime acquisitionDate;

    private LocalDateTime depreciationStartDate;

    private String department;

    private String custodian;

    private String location;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer delFlag;
}
