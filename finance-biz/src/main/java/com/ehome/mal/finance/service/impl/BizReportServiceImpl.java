package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAccount;
import com.ehome.mal.finance.api.entity.BizAccountBalance;
import com.ehome.mal.finance.api.entity.BizReportHistory;
import com.ehome.mal.finance.mapper.BizAccountMapper;
import com.ehome.mal.finance.mapper.BizReportHistoryMapper;
import com.ehome.mal.finance.service.BizAccountBalanceService;
import com.ehome.mal.finance.service.BizReportService;
import com.ehome.mal.finance.api.enums.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizReportServiceImpl extends ServiceImpl<BizReportHistoryMapper, BizReportHistory> implements BizReportService {

    private final BizAccountBalanceService accountBalanceService;
    private final BizAccountMapper accountMapper;

    @Override
    public BizReportHistory getReport(String tenantId, String reportType, String periodCode) {
        LambdaQueryWrapper<BizReportHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizReportHistory::getTenantId, tenantId)
                .eq(BizReportHistory::getReportType, reportType)
                .eq(BizReportHistory::getPeriodCode, periodCode);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizReportHistory generateReport(String tenantId, String reportType, String periodCode) {
        BizReportHistory existing = getReport(tenantId, reportType, periodCode);
        if (existing != null) {
            return existing;
        }

        List<BizAccountBalance> balances = accountBalanceService.getBalancesByPeriod(tenantId, periodCode);
        List<BizAccount> allAccounts = accountMapper.selectList(
                new LambdaQueryWrapper<BizAccount>()
                        .eq(BizAccount::getTenantId, tenantId)
                        );
        Map<Long, BizAccount> accountMap = allAccounts.stream()
                .collect(Collectors.toMap(BizAccount::getId, a -> a, (a, b) -> a));

        String reportContent = buildReportContent(tenantId, reportType, balances, accountMap);

        BizReportHistory report = new BizReportHistory();
        report.setTenantId(tenantId);
        report.setReportType(reportType);
        report.setPeriodCode(periodCode);
        report.setReportName(getReportName(reportType) + "-" + periodCode);
        report.setReportContent(reportContent);
        report.setCreateBy("system");
        report.setCreateTime(Instant.now());
        save(report);

        return report;
    }

    private String getReportName(String reportType) {
        for (ReportType rt : ReportType.values()) {
            if (rt.getCode().equals(reportType)) {
                return rt.getDesc();
            }
        }
        return reportType;
    }

    private String buildReportContent(String tenantId, String reportType,
                                       List<BizAccountBalance> balances,
                                       Map<Long, BizAccount> accountMap) {
        Map<Long, BizAccountBalance> balanceMap = balances.stream()
                .collect(Collectors.toMap(BizAccountBalance::getAccountId, b -> b, (a, b) -> a));

        if (ReportType.BALANCE_SHEET.getCode().equals(reportType)) {
            return buildBalanceSheet(balanceMap, accountMap);
        } else if (ReportType.PROFIT_LOSS.getCode().equals(reportType)) {
            return buildProfitLoss(tenantId, balanceMap, accountMap);
        } else if (ReportType.CASH_FLOW.getCode().equals(reportType)) {
            return buildCashFlow(tenantId, balanceMap, accountMap);
        } else if (ReportType.EQUITY_CHANGE.getCode().equals(reportType)) {
            return buildEquityChange(tenantId, balanceMap, accountMap);
        }
        return buildGenericReport(reportType, balances);
    }

    private String buildBalanceSheet(Map<Long, BizAccountBalance> balanceMap,
                                      Map<Long, BizAccount> accountMap) {
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        StringBuilder items = new StringBuilder();

        for (Map.Entry<Long, BizAccountBalance> entry : balanceMap.entrySet()) {
            BizAccount account = accountMap.get(entry.getKey());
            BizAccountBalance balance = entry.getValue();
            if (account == null) continue;

            String type = account.getAccountType();
            BigDecimal ending = balance.getEndingBalance() != null ? balance.getEndingBalance() : BigDecimal.ZERO;

            if ("asset".equals(type)) {
                totalAssets = totalAssets.add(ending);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"asset\",\"amount\":").append(ending).append("},");
            } else if ("liability".equals(type)) {
                totalLiabilities = totalLiabilities.add(ending);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"liability\",\"amount\":").append(ending).append("},");
            } else if ("equity".equals(type)) {
                totalEquity = totalEquity.add(ending);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"equity\",\"amount\":").append(ending).append("},");
            }
        }

        if (items.length() > 0) items.setLength(items.length() - 1);
        return "{\"reportType\":\"balance_sheet\",\"totalAssets\":" + totalAssets
                + ",\"totalLiabilities\":" + totalLiabilities
                + ",\"totalEquity\":" + totalEquity
                + ",\"liabilitiesAndEquity\":" + totalLiabilities.add(totalEquity)
                + ",\"items\":[" + items + "]}";
    }

    private String buildProfitLoss(String tenantId, Map<Long, BizAccountBalance> balanceMap,
                                    Map<Long, BizAccount> accountMap) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        StringBuilder items = new StringBuilder();

        for (Map.Entry<Long, BizAccountBalance> entry : balanceMap.entrySet()) {
            BizAccount account = accountMap.get(entry.getKey());
            BizAccountBalance balance = entry.getValue();
            if (account == null) continue;

            String type = account.getAccountType();
            if ("revenue".equals(type)) {
                BigDecimal amount = balance.getYearCredit() != null ? balance.getYearCredit() : BigDecimal.ZERO;
                totalRevenue = totalRevenue.add(amount);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"revenue\",\"amount\":").append(amount).append("},");
            } else if ("expense".equals(type)) {
                BigDecimal amount = balance.getYearDebit() != null ? balance.getYearDebit() : BigDecimal.ZERO;
                totalExpense = totalExpense.add(amount);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"expense\",\"amount\":").append(amount).append("},");
            }
        }

        if (items.length() > 0) items.setLength(items.length() - 1);
        BigDecimal netProfit = totalRevenue.subtract(totalExpense);
        return "{\"reportType\":\"profit_loss\",\"totalRevenue\":" + totalRevenue
                + ",\"totalExpense\":" + totalExpense
                + ",\"netProfit\":" + netProfit
                + ",\"items\":[" + items + "]}";
    }

    private String buildCashFlow(String tenantId, Map<Long, BizAccountBalance> balanceMap,
                                  Map<Long, BizAccount> accountMap) {
        List<BizAccount> cashAccounts = accountMapper.selectByAccountType(tenantId, "cash");
        BigDecimal operatingCashFlow = BigDecimal.ZERO;
        BigDecimal investingCashFlow = BigDecimal.ZERO;
        BigDecimal financingCashFlow = BigDecimal.ZERO;
        StringBuilder items = new StringBuilder();

        for (Map.Entry<Long, BizAccountBalance> entry : balanceMap.entrySet()) {
            BizAccount account = accountMap.get(entry.getKey());
            BizAccountBalance balance = entry.getValue();
            if (account == null) continue;

            String type = account.getAccountType();
            BigDecimal periodDebit = balance.getPeriodDebit() != null ? balance.getPeriodDebit() : BigDecimal.ZERO;
            BigDecimal periodCredit = balance.getPeriodCredit() != null ? balance.getPeriodCredit() : BigDecimal.ZERO;
            BigDecimal net = periodDebit.subtract(periodCredit);

            if ("cash_operating".equals(type)) {
                operatingCashFlow = operatingCashFlow.add(net);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"operating\",\"amount\":").append(net).append("},");
            } else if ("cash_investing".equals(type)) {
                investingCashFlow = investingCashFlow.add(net);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"investing\",\"amount\":").append(net).append("},");
            } else if ("cash_financing".equals(type)) {
                financingCashFlow = financingCashFlow.add(net);
                items.append("{\"accountCode\":\"").append(account.getAccountCode())
                        .append("\",\"accountName\":\"").append(account.getAccountName())
                        .append("\",\"category\":\"financing\",\"amount\":").append(net).append("},");
            }
        }

        if (items.length() > 0) items.setLength(items.length() - 1);
        BigDecimal netCashChange = operatingCashFlow.add(investingCashFlow).add(financingCashFlow);
        return "{\"reportType\":\"cash_flow\",\"operatingCashFlow\":" + operatingCashFlow
                + ",\"investingCashFlow\":" + investingCashFlow
                + ",\"financingCashFlow\":" + financingCashFlow
                + ",\"netCashChange\":" + netCashChange
                + ",\"items\":[" + items + "]}";
    }

    private String buildEquityChange(String tenantId, Map<Long, BizAccountBalance> balanceMap,
                                      Map<Long, BizAccount> accountMap) {
        BigDecimal beginningEquity = BigDecimal.ZERO;
        BigDecimal currentPeriodChange = BigDecimal.ZERO;
        BigDecimal endingEquity = BigDecimal.ZERO;
        StringBuilder items = new StringBuilder();

        for (Map.Entry<Long, BizAccountBalance> entry : balanceMap.entrySet()) {
            BizAccount account = accountMap.get(entry.getKey());
            BizAccountBalance balance = entry.getValue();
            if (account == null || !"equity".equals(account.getAccountType())) continue;

            BigDecimal beginning = balance.getBeginningBalance() != null ? balance.getBeginningBalance() : BigDecimal.ZERO;
            BigDecimal ending = balance.getEndingBalance() != null ? balance.getEndingBalance() : BigDecimal.ZERO;
            BigDecimal change = ending.subtract(beginning);

            beginningEquity = beginningEquity.add(beginning);
            currentPeriodChange = currentPeriodChange.add(change);
            endingEquity = endingEquity.add(ending);

            items.append("{\"accountCode\":\"").append(account.getAccountCode())
                    .append("\",\"accountName\":\"").append(account.getAccountName())
                    .append("\",\"beginning\":").append(beginning)
                    .append(",\"change\":").append(change)
                    .append(",\"ending\":").append(ending).append("},");
        }

        if (items.length() > 0) items.setLength(items.length() - 1);
        return "{\"reportType\":\"equity_change\",\"beginningEquity\":" + beginningEquity
                + ",\"currentPeriodChange\":" + currentPeriodChange
                + ",\"endingEquity\":" + endingEquity
                + ",\"items\":[" + items + "]}";
    }

    private String buildGenericReport(String reportType, List<BizAccountBalance> balances) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"reportType\":\"").append(reportType).append("\",\"items\":[");
        for (int i = 0; i < balances.size(); i++) {
            BizAccountBalance balance = balances.get(i);
            sb.append("{\"accountId\":").append(balance.getAccountId())
                    .append(",\"endingBalance\":").append(balance.getEndingBalance()).append("}");
            if (i < balances.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public byte[] exportReport(String tenantId, String reportType, String periodCode, String format) {
        BizReportHistory report = getReport(tenantId, reportType, periodCode);
        if (report == null) {
            report = generateReport(tenantId, reportType, periodCode);
        }

        if ("excel".equalsIgnoreCase(format)) {
            return exportAsCsv(report);
        }
        return exportAsText(report);
    }

    private byte[] exportAsCsv(BizReportHistory report) {
        StringBuilder csv = new StringBuilder();
        csv.append("报表类型,期间,报表名称\n");
        csv.append(report.getReportType()).append(",")
                .append(report.getPeriodCode()).append(",")
                .append(report.getReportName()).append("\n\n");
        csv.append("报表内容\n");
        csv.append(report.getReportContent());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] exportAsText(BizReportHistory report) {
        StringBuilder text = new StringBuilder();
        text.append("报表名称: ").append(report.getReportName()).append("\n");
        text.append("报表类型: ").append(report.getReportType()).append("\n");
        text.append("期间: ").append(report.getPeriodCode()).append("\n");
        text.append("生成时间: ").append(report.getCreateTime()).append("\n\n");
        text.append(report.getReportContent());
        return text.toString().getBytes(StandardCharsets.UTF_8);
    }
}