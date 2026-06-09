package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizVoucherEntry;
import com.ehome.mal.finance.service.BizVoucherEntryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "明细账查询")
@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final BizVoucherEntryService voucherEntryService;

    @ApiOperation("按科目查询明细账")
    @GetMapping("/detail")
    public R<List<BizVoucherEntry>> getDetailLedger(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                                     @RequestParam Long accountId,
                                                     @RequestParam String periodCode) {
        return R.ok(voucherEntryService.getEntriesByAccountIdAndPeriod(tenantId, accountId, periodCode));
    }
}