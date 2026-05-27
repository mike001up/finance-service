package com.ehome.mal.financeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_depreciation_detail")
public class BizDepreciationDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private Long assetId;

    private String periodCode;

    private BigDecimal depreciationAmount;

    private BigDecimal accumulatedDepreciation;

    private BigDecimal netValue;

    private Long voucherId;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer delFlag;
}
