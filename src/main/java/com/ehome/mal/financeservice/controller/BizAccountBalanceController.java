package com.ehome.mal.financeservice.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.financeservice.entity.BizAccountBalance;
import com.ehome.mal.financeservice.service.BizAccountBalanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "科目余额管理")
@RestController
@RequestMapping("/account-balance")
@RequiredArgsConstructor
public class BizAccountBalanceController {

    private final BizAccountBalanceService accountBalanceService;

    @ApiOperation("查询科目余额")
    @GetMapping("/list")
    public R<List<BizAccountBalance>> getBalancesByPeriod(@RequestParam String tenantId,
                                                           @RequestParam String periodCode) {
        return R.ok(accountBalanceService.getBalancesByPeriod(tenantId, periodCode));
    }

    @ApiOperation("试算平衡")
    @GetMapping("/trialBalance")
    public R<Boolean> calculateTrialBalance(@RequestParam String tenantId,
                                             @RequestParam String periodCode) {
        return R.ok(accountBalanceService.calculateTrialBalance(tenantId, periodCode));
    }
}
