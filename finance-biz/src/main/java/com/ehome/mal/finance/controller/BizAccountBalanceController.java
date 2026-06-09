package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizAccountBalance;
import com.ehome.mal.finance.service.BizAccountBalanceService;
import com.ehome.mal.finance.api.vo.TrialBalanceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Api(tags = "科目余额管理")
@RestController
@RequestMapping("/account-balance")
@RequiredArgsConstructor
public class BizAccountBalanceController {

    private final BizAccountBalanceService accountBalanceService;

    @ApiOperation("查询科目余额")
    @GetMapping("/list")
    public R<List<BizAccountBalance>> getBalancesByPeriod(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                                           @RequestParam String periodCode) {
        return R.ok(accountBalanceService.getBalancesByPeriod(tenantId, periodCode));
    }

    @ApiOperation("导入期初余额（JSON）")
    @PostMapping("/import")
    public R<Boolean> importBeginningBalances(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                               @RequestParam String periodCode,
                                               @RequestBody List<BizAccountBalance> balances) {
        return R.ok(accountBalanceService.importBeginningBalances(tenantId, periodCode, balances));
    }

    @ApiOperation("下载期初余额导入模板")
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId) {
        byte[] data = accountBalanceService.generateImportTemplate(tenantId);
        String filename = URLEncoder.encode("期初余额导入模板.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @ApiOperation("通过Excel导入期初余额")
    @PostMapping("/import/excel")
    public R<Map<String, Object>> importFromExcel(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                                   @RequestParam String periodCode,
                                                   @RequestParam("file") MultipartFile file) {
        return R.ok(accountBalanceService.importFromExcel(tenantId, periodCode, file));
    }

    @ApiOperation("试算平衡")
    @GetMapping("/trialBalance")
    public R<TrialBalanceVO> calculateTrialBalance(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                                    @RequestParam String periodCode) {
        return R.ok(accountBalanceService.calculateTrialBalance(tenantId, periodCode));
    }
}
