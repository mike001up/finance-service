package com.ehome.mal.finance.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.pig4cloud.pig.common.core.constant.enums.IsDelEnum;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@TableName("biz_prepaid_expense_amortization")
public class BizPrepaidExpenseAmortization implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String tenantId;

    private Long prepaidVoucherId;

    private String amortizationNo;

    private String amortizationName;

    private Long accountId;

    private Long expenseAccountId;

    private BigDecimal totalAmount;

    private BigDecimal amortizedAmount;

    private BigDecimal remainingAmount;

    private Integer totalPeriods;

    private Integer amortizedPeriods;

    private String startPeriod;

    private String endPeriod;

    private Instant startDate;

    private Instant endDate;

    private String amortizationMethod;

    private String remark;

    private Integer status;

    private Instant createTime;

    private Instant updateTime;

    private String createBy;

    private String updateBy;
	@TableLogic(value = "'NO'", delval = "'YES'")
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,YES:已删除,NO:正常")
	private IsDelEnum isDel;
}
