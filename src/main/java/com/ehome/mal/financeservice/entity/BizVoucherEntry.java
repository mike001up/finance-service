package com.ehome.mal.financeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_voucher_entry")
public class BizVoucherEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer delFlag;
}
