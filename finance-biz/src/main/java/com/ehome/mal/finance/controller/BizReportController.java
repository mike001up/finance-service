package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizReportHistory;
import com.ehome.mal.finance.service.BizReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "财务报表")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class BizReportController {

    private final BizReportService reportService;

    @ApiOperation("查询报表")
    @GetMapping("/{reportType}")
    public R<BizReportHistory> getReport(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                         @PathVariable String reportType,
                                         @RequestParam String periodCode) {
        BizReportHistory report = reportService.getReport(tenantId, reportType, periodCode);
        if (report == null) {
            return R.failed("报表生成中，请稍后再试");
        }
        return R.ok(report);
    }

    @ApiOperation("生成报表")
    @PostMapping("/generate/{reportType}")
    public R<BizReportHistory> generateReport(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                              @PathVariable String reportType,
                                              @RequestParam String periodCode) {
        return R.ok(reportService.generateReport(tenantId, reportType, periodCode));
    }

    @ApiOperation("导出报表")
    @GetMapping("/export/{reportType}")
    public ResponseEntity<byte[]> exportReport(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                                @PathVariable String reportType,
                                                @RequestParam String periodCode,
                                                @RequestParam(defaultValue = "excel") String format) {
        byte[] data = reportService.exportReport(tenantId, reportType, periodCode, format);
        String filename = reportType + "_" + periodCode + "." + ("excel".equalsIgnoreCase(format) ? "csv" : "txt");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}