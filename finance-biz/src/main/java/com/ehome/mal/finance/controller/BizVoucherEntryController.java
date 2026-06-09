package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizVoucherEntry;
import com.ehome.mal.finance.service.BizVoucherEntryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "凭证分录管理")
@RestController
@RequestMapping("/voucher-entry")
@RequiredArgsConstructor
public class BizVoucherEntryController {

    private final BizVoucherEntryService voucherEntryService;

    @ApiOperation("根据凭证ID查询分录")
    @GetMapping("/list/{voucherId}")
    public R<List<BizVoucherEntry>> getEntriesByVoucherId(@PathVariable Long voucherId) {
        return R.ok(voucherEntryService.getEntriesByVoucherId(voucherId));
    }

    @ApiOperation("保存凭证分录")
    @PostMapping("/save/{voucherId}")
    public R<Boolean> saveEntries(@PathVariable Long voucherId, @RequestBody List<BizVoucherEntry> entries) {
        if (!voucherEntryService.checkBalance(entries)) {
            return R.failed("借贷不平衡");
        }
        return R.ok(voucherEntryService.saveEntries(voucherId, entries));
    }

    @ApiOperation("校验借贷平衡")
    @PostMapping("/checkBalance")
    public R<Boolean> checkBalance(@RequestBody List<BizVoucherEntry> entries) {
        return R.ok(voucherEntryService.checkBalance(entries));
    }
}
