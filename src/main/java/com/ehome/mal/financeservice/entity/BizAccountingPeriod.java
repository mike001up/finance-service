package com.ehome.mal.financeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_accounting_period")
public class BizAccountingPeriod implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String periodCode;

    private String periodName;

    private Integer year;

    private Integer month;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer status;

    private Integer isAdjust;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer delFlag;
}
