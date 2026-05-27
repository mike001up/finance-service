package com.ehome.mal.financeservice.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoucherDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String voucherNo;

    private String periodCode;

    private String voucherType;

    private LocalDateTime voucherDate;

    private Integer voucherCount;

    private String attachmentCount;

    private String maker;

    private String reviewer;

    private LocalDateTime reviewTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private List<VoucherEntryVO> entries;
}
