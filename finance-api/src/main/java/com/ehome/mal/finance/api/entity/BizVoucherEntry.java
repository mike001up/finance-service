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
@TableName("biz_voucher_entry")
public class BizVoucherEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String tenantId;

    private Long voucherId;

    private Integer entrySeq;

    private String summary;

    private Long accountId;

    private BigDecimal debitAmount;

    private BigDecimal creditAmount;

    private String auxiliary1Type;

    private String auxiliary1Value;

    private String auxiliary2Type;

    private String auxiliary2Value;

    private String auxiliary3Type;

    private String auxiliary3Value;

    private String auxiliary4Type;

    private String auxiliary4Value;

    private Instant createTime;

    private Instant updateTime;
	@TableLogic(value = "'NO'", delval = "'YES'")
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,YES:已删除,NO:正常")
	private IsDelEnum isDel;
}
