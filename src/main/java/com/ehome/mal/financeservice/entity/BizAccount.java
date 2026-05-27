package com.ehome.mal.financeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_account")
public class BizAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String accountCode;

    private String accountName;

    private String accountType;

    private Long parentId;

    private Integer level;

    private String direction;

    private BigDecimal initialDebit;

    private BigDecimal initialCredit;

    private Integer auxiliaryFlag;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer delFlag;

    @TableField(exist = false)
    private String parentName;
}
