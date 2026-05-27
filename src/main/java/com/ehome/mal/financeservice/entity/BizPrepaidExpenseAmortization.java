package com.ehome.mal.financeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_prepaid_expense_amortization")
public class BizPrepaidExpenseAmortization implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String amortizationNo;

    private String amortizationName;

    private Long accountId;

    private BigDecimal totalAmount;

    private BigDecimal amortizedAmount;

    private BigDecimal remainingAmount;

    private Integer totalPeriods;

    private Integer amortizedPeriods;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String amortizationMethod;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer delFlag;
}
