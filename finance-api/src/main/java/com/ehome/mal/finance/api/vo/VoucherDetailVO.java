package com.ehome.mal.finance.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VoucherDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String voucherNo;

    private String periodCode;

    private String voucherType;

    private String voucherDate;

    private Integer voucherCount;

    private String attachmentCount;

    private String maker;

    private String reviewer;

    private String reviewTime;

    private Integer status;

    private String remark;

    private String createTime;

    private List<VoucherEntryVO> entries;
}
