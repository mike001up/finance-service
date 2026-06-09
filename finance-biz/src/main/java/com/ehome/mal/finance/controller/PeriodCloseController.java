package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.service.PeriodCloseService;
import com.ehome.mal.finance.api.vo.TrialBalanceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "期末处理")
@RestController
@RequestMapping("/period-close")
@RequiredArgsConstructor
public class PeriodCloseController {

    private final PeriodCloseService periodCloseService;

    @ApiOperation("计提应收利息")
    @PostMapping("/accrue-interest")
    public R<Long> accrueInterest(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                  @RequestParam String periodCode) {
        return R.ok(periodCloseService.accrueInterest(tenantId, periodCode));
    }

    @ApiOperation("计提固定资产折旧")
    @PostMapping("/depreciation")
    public R<Long> depreciation(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                @RequestParam String periodCode) {
        return R.ok(periodCloseService.depreciation(tenantId, periodCode));
    }

    @ApiOperation("摊销预付费用")
    @PostMapping("/prepaid-amortization")
    public R<Long> prepaidAmortization(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                       @RequestParam String periodCode) {
        return R.ok(periodCloseService.prepaidAmortization(tenantId, periodCode));
    }

    @ApiOperation("计提预提费用")
    @PostMapping("/accrual-expense")
    public R<Long> accrualExpense(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                  @RequestParam String periodCode) {
        return R.ok(periodCloseService.accrualExpense(tenantId, periodCode));
    }

    @ApiOperation("计提贷款损失准备")
    @PostMapping("/credit-loss")
    public R<Long> creditLoss(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                              @RequestParam String periodCode) {
        return R.ok(periodCloseService.creditLoss(tenantId, periodCode));
    }

    @ApiOperation("结转损益")
    @PostMapping("/write-off")
    public R<Long> writeOff(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                            @RequestParam String periodCode) {
        return R.ok(periodCloseService.writeOff(tenantId, periodCode));
    }

    @ApiOperation("试算平衡检查")
    @PostMapping("/trial-balance")
    public R<TrialBalanceVO> trialBalance(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                          @RequestParam String periodCode) {
        return R.ok(periodCloseService.trialBalance(tenantId, periodCode));
    }

    @ApiOperation("关闭期间")
    @PostMapping("/close-period")
    public R<Boolean> closePeriod(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                  @RequestParam String periodCode,
                                  @RequestParam String closedBy) {
        return R.ok(periodCloseService.closePeriod(tenantId, periodCode, closedBy));
    }

    @ApiOperation("年度结转")
    @PostMapping("/year-end-transfer")
    public R<Boolean> yearEndTransfer(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                      @RequestParam String periodCode) {
        return R.ok(periodCloseService.yearEndTransfer(tenantId, periodCode));
    }
}