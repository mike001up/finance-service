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
@TableName("biz_interest_accrual_detail")
public class BizInterestAccrualDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String tenantId;

    private String loanNo;

    private String periodCode;

    private BigDecimal principalBalance;

    private BigDecimal interestRate;

    private Integer accrualDays;

    private BigDecimal interestAmount;

    private Long voucherId;

    private String remark;

    private Instant createTime;

    private Instant updateTime;
	@TableLogic(value = "'NO'", delval = "'YES'")
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,YES:已删除,NO:正常")
	private IsDelEnum isDel;
}
