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
@TableName("biz_fixed_asset")
public class BizFixedAsset implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String tenantId;

    private String assetCode;

    private String assetName;

    private String assetCategory;

    private Long accountDebitId;

    private Long accountCreditId;

    private BigDecimal originalValue;

    private BigDecimal salvageValue;

    private BigDecimal residualRate;

    private BigDecimal depreciationValue;

    private BigDecimal netValue;

    private Integer usefulLife;

    private String depreciationMethod;

    private Instant acquisitionDate;

    private Instant depreciationStartDate;

    private String department;

    private String custodian;

    private String location;

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
