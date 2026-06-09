package com.ehome.mal.finance.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class VoucherDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    @NotBlank(message = "会计期间不能为空")
    private String periodCode;

    @NotBlank(message = "凭证类型不能为空")
    private String voucherType;

    @NotNull(message = "凭证日期不能为空")
    private Instant voucherDate;

    private String attachmentCount;

    private String remark;

    @NotNull(message = "凭证分录不能为空")
    private List<VoucherEntryDTO> entries;
}
