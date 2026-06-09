package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizAccountingPeriod;
import com.ehome.mal.finance.service.BizAccountingPeriodService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "会计期间管理")
@RestController
@RequestMapping("/accounting-period")
@RequiredArgsConstructor
public class BizAccountingPeriodController {

    private final BizAccountingPeriodService accountingPeriodService;

    @ApiOperation("查询会计期间列表")
    @GetMapping("/list")
    public R<List<BizAccountingPeriod>> getPeriodsByTenant(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId) {
        return R.ok(accountingPeriodService.getPeriodsByTenant(tenantId));
    }

    @ApiOperation("开启会计期间")
    @PostMapping("/open")
    public R<Boolean> openPeriod(@RequestParam String periodCode) {
        return R.ok(accountingPeriodService.openPeriod(periodCode));
    }

    @ApiOperation("关闭会计期间")
    @PostMapping("/close")
    public R<Boolean> closePeriod(@RequestParam String periodCode,
                                  @RequestParam String closedBy) {
        return R.ok(accountingPeriodService.closePeriod(periodCode, closedBy));
    }

    @ApiOperation("获取当前会计期间")
    @GetMapping("/current")
    public R<BizAccountingPeriod> getCurrentPeriod(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId) {
        return R.ok(accountingPeriodService.getCurrentPeriod(tenantId));
    }
}
