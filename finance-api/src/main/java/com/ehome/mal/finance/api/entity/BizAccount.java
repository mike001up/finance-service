package com.ehome.mal.finance.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.pig4cloud.pig.common.core.constant.enums.IsDelEnum;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@TableName("biz_account")
public class BizAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
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

    private Integer isEnabled;

    private Integer status;

    private String remark;

    private Instant createTime;

    private Instant updateTime;

    private String createBy;

    private String updateBy;
	@TableLogic(value = "'NO'", delval = "'YES'")
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,YES:已删除,NO:正常")
	private IsDelEnum isDel;

    @TableField(exist = false)
    private String parentName;
}
