package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizReportHistory;

public interface BizReportService extends IService<BizReportHistory> {
    BizReportHistory getReport(String tenantId, String reportType, String periodCode);

    BizReportHistory generateReport(String tenantId, String reportType, String periodCode);

    byte[] exportReport(String tenantId, String reportType, String periodCode, String format);
}