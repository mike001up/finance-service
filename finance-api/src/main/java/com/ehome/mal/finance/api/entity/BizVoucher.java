package com.ehome.mal.finance.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.pig4cloud.pig.common.core.constant.enums.IsDelEnum;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
@TableName("biz_voucher")
public class BizVoucher implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String tenantId;

    private String voucherNo;

    private String periodCode;

    private String voucherType;

    private Instant voucherDate;

    private Integer voucherCount;

    private Integer attachmentCount;

    private String attachmentUrl;

    private String maker;

    private String reviewer;

    private Instant reviewTime;

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
}
