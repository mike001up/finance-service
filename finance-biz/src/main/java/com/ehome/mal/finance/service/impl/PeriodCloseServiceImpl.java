package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ehome.mal.finance.api.entity.*;
import com.ehome.mal.finance.api.enums.AmortizationStatus;
import com.ehome.mal.finance.api.enums.DepreciationMethod;
import com.ehome.mal.finance.api.enums.FixedAssetStatus;
import com.ehome.mal.finance.api.enums.PeriodStatus;
import com.ehome.mal.finance.mapper.*;
import com.ehome.mal.finance.service.BizAccountBalanceService;
import com.ehome.mal.finance.service.BizAccountingPeriodService;
import com.ehome.mal.finance.service.PeriodCloseService;
import com.ehome.mal.finance.api.vo.TrialBalanceVO;
import com.ehome.mal.finance.event.AuditLogPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodCloseServiceImpl implements PeriodCloseService {

    private final BizFixedAssetMapper fixedAssetMapper;
    private final BizDepreciationDetailMapper depreciationDetailMapper;
    private final BizVoucherMapper voucherMapper;
    private final BizVoucherEntryMapper voucherEntryMapper;
    private final BizAccountBalanceService accountBalanceService;
    private final BizAccountingPeriodService accountingPeriodService;
    private final BizPrepaidExpenseAmortizationMapper prepaidAmortizationMapper;
    private final BizPrepaidAmortizationDetailMapper prepaidAmortizationDetailMapper;
    private final BizAccrualExpenseDetailMapper accrualExpenseDetailMapper;
    private final BizCreditLossDetailMapper creditLossDetailMapper;
    private final BizInterestAccrualDetailMapper interestAccrualDetailMapper;
    private final BizAccountMapper accountMapper;
    private final AuditLogPublisher auditLogPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long accrueInterest(String tenantId, String periodCode) {
        List<BizInterestAccrualDetail> details = interestAccrualDetailMapper.selectList(
                new LambdaQueryWrapper<BizInterestAccrualDetail>()
                        .eq(BizInterestAccrualDetail::getTenantId, tenantId)
                        .eq(BizInterestAccrualDetail::getPeriodCode, periodCode));

        if (details.isEmpty()) {
            return null;
        }

        BigDecimal totalInterest = details.stream()
                .map(BizInterestAccrualDetail::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BizVoucher voucher = createAutoVoucher(tenantId, periodCode, "计提" + periodCode + "月贷款利息", totalInterest);
        voucherMapper.insert(voucher);

        for (BizInterestAccrualDetail detail : details) {
            detail.setVoucherId(voucher.getId());
            detail.setUpdateTime(Instant.now());
            interestAccrualDetailMapper.updateById(detail);
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long depreciation(String tenantId, String periodCode) {
        List<BizFixedAsset> assets = fixedAssetMapper.selectList(
                new LambdaQueryWrapper<BizFixedAsset>()
                        .eq(BizFixedAsset::getTenantId, tenantId)
                        .eq(BizFixedAsset::getStatus, FixedAssetStatus.IN_SERVICE.getCode()));

        if (assets.isEmpty()) {
            return null;
        }

        BigDecimal totalDepreciation = BigDecimal.ZERO;

        for (BizFixedAsset asset : assets) {
            BigDecimal depreciableAmount = asset.getOriginalValue().subtract(asset.getSalvageValue());
            BigDecimal depAmount;

            if (DepreciationMethod.STRAIGHT_LINE.getCode().equals(asset.getDepreciationMethod())) {
                depAmount = depreciableAmount.divide(new BigDecimal(asset.getUsefulLife()), 2, RoundingMode.HALF_UP);
            } else if (DepreciationMethod.DOUBLE_DECLINING.getCode().equals(asset.getDepreciationMethod())) {
                BigDecimal rate = new BigDecimal("2").divide(new BigDecimal(asset.getUsefulLife()), 4, RoundingMode.HALF_UP);
                depAmount = asset.getNetValue().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            } else {
                continue;
            }

            BigDecimal accumulated = asset.getDepreciationValue().add(depAmount);
            BigDecimal netVal = asset.getOriginalValue().subtract(accumulated);

            asset.setDepreciationValue(accumulated);
            asset.setNetValue(netVal);
            asset.setUpdateTime(Instant.now());
            fixedAssetMapper.updateById(asset);

            BizDepreciationDetail detail = new BizDepreciationDetail();
            detail.setTenantId(tenantId);
            detail.setAssetId(asset.getId());
            detail.setPeriodCode(periodCode);
            detail.setDepreciationAmount(depAmount);
            detail.setAccumulatedDepreciation(accumulated);
            detail.setNetValue(netVal);
            detail.setDepartment(asset.getDepartment());
            detail.setExpenseAccountId(asset.getAccountDebitId());
            detail.setCreateTime(Instant.now());
            depreciationDetailMapper.insert(detail);

            totalDepreciation = totalDepreciation.add(depAmount);
        }

        if (totalDepreciation.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BizVoucher voucher = createAutoVoucher(tenantId, periodCode, "计提" + periodCode + "月固定资产折旧", totalDepreciation);
        voucherMapper.insert(voucher);

        List<BizDepreciationDetail> depDetails = depreciationDetailMapper.selectList(
                new LambdaQueryWrapper<BizDepreciationDetail>()
                        .eq(BizDepreciationDetail::getTenantId, tenantId)
                        .eq(BizDepreciationDetail::getPeriodCode, periodCode));
        for (BizDepreciationDetail d : depDetails) {
            d.setVoucherId(voucher.getId());
            d.setUpdateTime(Instant.now());
            depreciationDetailMapper.updateById(d);
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long prepaidAmortization(String tenantId, String periodCode) {
        List<BizPrepaidExpenseAmortization> amortizations = prepaidAmortizationMapper.selectList(
                new LambdaQueryWrapper<BizPrepaidExpenseAmortization>()
                        .eq(BizPrepaidExpenseAmortization::getTenantId, tenantId)
                        .in(BizPrepaidExpenseAmortization::getStatus,
                                AmortizationStatus.NOT_STARTED.getCode(),
                                AmortizationStatus.IN_PROGRESS.getCode())
                        .le(BizPrepaidExpenseAmortization::getStartPeriod, periodCode)
                        .ge(BizPrepaidExpenseAmortization::getEndPeriod, periodCode));

        if (amortizations.isEmpty()) {
            return null;
        }

        BigDecimal totalAmortization = BigDecimal.ZERO;

        for (BizPrepaidExpenseAmortization amort : amortizations) {
            BigDecimal periodAmount;
            if ("AVERAGE_MONTH".equals(amort.getAmortizationMethod())) {
                periodAmount = amort.getTotalAmount().divide(
                        new BigDecimal(amort.getTotalPeriods()), 2, RoundingMode.HALF_UP);
            } else {
                periodAmount = amort.getRemainingAmount();
            }

            amort.setAmortizedAmount(amort.getAmortizedAmount().add(periodAmount));
            amort.setRemainingAmount(amort.getRemainingAmount().subtract(periodAmount));
            amort.setAmortizedPeriods(amort.getAmortizedPeriods() + 1);
            if (amort.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                amort.setStatus(AmortizationStatus.COMPLETED.getCode());
            } else {
                amort.setStatus(AmortizationStatus.IN_PROGRESS.getCode());
            }
            amort.setUpdateTime(Instant.now());
            prepaidAmortizationMapper.updateById(amort);

            BizPrepaidAmortizationDetail detail = new BizPrepaidAmortizationDetail();
            detail.setTenantId(tenantId);
            detail.setAmortizationId(amort.getId());
            detail.setPeriodCode(periodCode);
            detail.setAmortizationAmount(periodAmount);
            detail.setAmortizationDesc(amort.getAmortizationName() + "-" + periodCode + "月摊销");
            detail.setCreateTime(Instant.now());
            prepaidAmortizationDetailMapper.insert(detail);

            totalAmortization = totalAmortization.add(periodAmount);
        }

        if (totalAmortization.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BizVoucher voucher = createAutoVoucher(tenantId, periodCode, "摊销" + periodCode + "月预付费用", totalAmortization);
        voucherMapper.insert(voucher);

        List<BizPrepaidAmortizationDetail> pdetails = prepaidAmortizationDetailMapper.selectList(
                new LambdaQueryWrapper<BizPrepaidAmortizationDetail>()
                        .eq(BizPrepaidAmortizationDetail::getTenantId, tenantId)
                        .eq(BizPrepaidAmortizationDetail::getPeriodCode, periodCode));
        for (BizPrepaidAmortizationDetail d : pdetails) {
            d.setVoucherId(voucher.getId());
            d.setUpdateTime(Instant.now());
            prepaidAmortizationDetailMapper.updateById(d);
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long accrualExpense(String tenantId, String periodCode) {
        List<BizAccrualExpenseDetail> details = accrualExpenseDetailMapper.selectList(
                new LambdaQueryWrapper<BizAccrualExpenseDetail>()
                        .eq(BizAccrualExpenseDetail::getTenantId, tenantId)
                        .eq(BizAccrualExpenseDetail::getPeriodCode, periodCode));

        if (details.isEmpty()) {
            return null;
        }

        BigDecimal totalAmount = details.stream()
                .map(BizAccrualExpenseDetail::getAccrualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BizVoucher voucher = createAutoVoucher(tenantId, periodCode, "计提" + periodCode + "月预提费用", totalAmount);
        voucherMapper.insert(voucher);

        for (BizAccrualExpenseDetail detail : details) {
            detail.setVoucherId(voucher.getId());
            detail.setUpdateTime(Instant.now());
            accrualExpenseDetailMapper.updateById(detail);
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long creditLoss(String tenantId, String periodCode) {
        List<BizCreditLossDetail> details = creditLossDetailMapper.selectList(
                new LambdaQueryWrapper<BizCreditLossDetail>()
                        .eq(BizCreditLossDetail::getTenantId, tenantId)
                        .eq(BizCreditLossDetail::getPeriodCode, periodCode));

        if (details.isEmpty()) {
            return null;
        }

        BigDecimal totalAdjustment = details.stream()
                .map(BizCreditLossDetail::getAdjustmentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BizVoucher voucher = createAutoVoucher(tenantId, periodCode, "计提" + periodCode + "月贷款损失准备", totalAdjustment.abs());
        voucherMapper.insert(voucher);

        for (BizCreditLossDetail detail : details) {
            detail.setVoucherId(voucher.getId());
            detail.setUpdateTime(Instant.now());
            creditLossDetailMapper.updateById(detail);
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long writeOff(String tenantId, String periodCode) {
        List<BizAccount> revenueAccounts = accountMapper.selectByAccountType(tenantId, "revenue");
        List<BizAccount> expenseAccounts = accountMapper.selectByAccountType(tenantId, "expense");

        List<BizAccountBalance> balances = accountBalanceService.getBalancesByPeriod(tenantId, periodCode);
        Map<Long, BizAccountBalance> balanceMap = balances.stream()
                .collect(Collectors.toMap(BizAccountBalance::getAccountId, b -> b, (a, b) -> a));

        BizAccount profitLossAccount = findProfitLossAccount(tenantId);
        if (profitLossAccount == null) {
            throw new IllegalStateException("未找到本年利润科目，请先配置");
        }

        BizVoucher voucher = new BizVoucher();
        voucher.setTenantId(tenantId);
        voucher.setPeriodCode(periodCode);
        voucher.setVoucherType("auto");
        voucher.setVoucherDate(Instant.now());
        voucher.setStatus(0);
        voucher.setMaker("system");
        voucher.setRemark("结转" + periodCode + "月损益");
        voucher.setCreateTime(Instant.now());
        voucher.setUpdateTime(Instant.now());
        voucherMapper.insert(voucher);

        List<BizVoucherEntry> entries = new ArrayList<>();
        int seq = 1;
        BigDecimal totalRevenueCredit = BigDecimal.ZERO;
        BigDecimal totalExpenseDebit = BigDecimal.ZERO;

        for (BizAccount revenueAcct : revenueAccounts) {
            BizAccountBalance bal = balanceMap.get(revenueAcct.getId());
            if (bal == null || bal.getYearCredit() == null || bal.getYearCredit().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal amount = bal.getYearCredit();
            BizVoucherEntry debitEntry = new BizVoucherEntry();
            debitEntry.setTenantId(tenantId);
            debitEntry.setVoucherId(voucher.getId());
            debitEntry.setEntrySeq(seq++);
            debitEntry.setSummary("结转收入-" + revenueAcct.getAccountName());
            debitEntry.setAccountId(revenueAcct.getId());
            debitEntry.setDebitAmount(amount);
            debitEntry.setCreditAmount(BigDecimal.ZERO);
            debitEntry.setCreateTime(Instant.now());
            entries.add(debitEntry);
            totalRevenueCredit = totalRevenueCredit.add(amount);
        }

        if (totalRevenueCredit.compareTo(BigDecimal.ZERO) > 0) {
            BizVoucherEntry creditEntry = new BizVoucherEntry();
            creditEntry.setTenantId(tenantId);
            creditEntry.setVoucherId(voucher.getId());
            creditEntry.setEntrySeq(seq++);
            creditEntry.setSummary("结转收入至本年利润");
            creditEntry.setAccountId(profitLossAccount.getId());
            creditEntry.setDebitAmount(BigDecimal.ZERO);
            creditEntry.setCreditAmount(totalRevenueCredit);
            creditEntry.setCreateTime(Instant.now());
            entries.add(creditEntry);
        }

        for (BizAccount expenseAcct : expenseAccounts) {
            BizAccountBalance bal = balanceMap.get(expenseAcct.getId());
            if (bal == null || bal.getYearDebit() == null || bal.getYearDebit().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal amount = bal.getYearDebit();
            BizVoucherEntry creditEntry = new BizVoucherEntry();
            creditEntry.setTenantId(tenantId);
            creditEntry.setVoucherId(voucher.getId());
            creditEntry.setEntrySeq(seq++);
            creditEntry.setSummary("结转费用-" + expenseAcct.getAccountName());
            creditEntry.setAccountId(expenseAcct.getId());
            creditEntry.setDebitAmount(BigDecimal.ZERO);
            creditEntry.setCreditAmount(amount);
            creditEntry.setCreateTime(Instant.now());
            entries.add(creditEntry);
            totalExpenseDebit = totalExpenseDebit.add(amount);
        }

        if (totalExpenseDebit.compareTo(BigDecimal.ZERO) > 0) {
            BizVoucherEntry debitEntry = new BizVoucherEntry();
            debitEntry.setTenantId(tenantId);
            debitEntry.setVoucherId(voucher.getId());
            debitEntry.setEntrySeq(seq++);
            debitEntry.setSummary("结转费用至本年利润");
            debitEntry.setAccountId(profitLossAccount.getId());
            debitEntry.setDebitAmount(totalExpenseDebit);
            debitEntry.setCreditAmount(BigDecimal.ZERO);
            debitEntry.setCreateTime(Instant.now());
            entries.add(debitEntry);
        }

        if (entries.isEmpty()) {
            voucherMapper.deleteById(voucher.getId());
            return null;
        }

        for (BizVoucherEntry entry : entries) {
            voucherEntryMapper.insert(entry);
        }

        auditLogPublisher.publish(tenantId, "period_close", "write_off", "system", "结转损益期间: " + periodCode + ", 凭证ID: " + voucher.getId());
        return voucher.getId();
    }

    private BizAccount findProfitLossAccount(String tenantId) {
        List<BizAccount> accounts = accountMapper.selectByAccountType(tenantId, "profit_loss");
        if (accounts != null && !accounts.isEmpty()) {
            return accounts.get(0);
        }
        LambdaQueryWrapper<BizAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccount::getTenantId, tenantId)
                .eq(BizAccount::getAccountCode, "3101")
                ;
        return accountMapper.selectOne(wrapper);
    }

    @Override
    public TrialBalanceVO trialBalance(String tenantId, String periodCode) {
        return accountBalanceService.calculateTrialBalance(tenantId, periodCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePeriod(String tenantId, String periodCode, String closedBy) {
        TrialBalanceVO trialResult = accountBalanceService.calculateTrialBalance(tenantId, periodCode);
        if (!trialResult.isBalanced()) {
            throw new IllegalStateException("试算不平衡，不能关账。差异：" + trialResult.getDifferences());
        }
        boolean result = accountingPeriodService.closePeriod(periodCode, closedBy);
        if (result) {
            auditLogPublisher.publish(tenantId, "period_close", "close_period", closedBy, "关账期间: " + periodCode);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean yearEndTransfer(String tenantId, String periodCode) {
        int month = Integer.parseInt(periodCode.substring(4));
        if (month != 12) {
            throw new IllegalStateException("年度结转仅在12月期间执行");
        }

        BizAccount profitLossAccount = findProfitLossAccount(tenantId);
        if (profitLossAccount == null) {
            throw new IllegalStateException("未找到本年利润科目，请先配置");
        }

        BizAccount retainedEarningsAccount = findRetainedEarningsAccount(tenantId);
        if (retainedEarningsAccount == null) {
            throw new IllegalStateException("未找到利润分配-未分配利润科目，请先配置");
        }

        List<BizAccountBalance> balances = accountBalanceService.getBalancesByPeriod(tenantId, periodCode);
        Map<Long, BizAccountBalance> balanceMap = balances.stream()
                .collect(Collectors.toMap(BizAccountBalance::getAccountId, b -> b, (a, b) -> a));

        BizAccountBalance plBalance = balanceMap.get(profitLossAccount.getId());
        if (plBalance == null || plBalance.getEndingBalance() == null
                || plBalance.getEndingBalance().compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        BigDecimal profitAmount = plBalance.getEndingBalance();

        BizVoucher voucher = new BizVoucher();
        voucher.setTenantId(tenantId);
        voucher.setPeriodCode(periodCode);
        voucher.setVoucherType("auto");
        voucher.setVoucherDate(Instant.now());
        voucher.setStatus(0);
        voucher.setMaker("system");
        voucher.setRemark("年度结转-本年利润结转至未分配利润");
        voucher.setCreateTime(Instant.now());
        voucher.setUpdateTime(Instant.now());
        voucherMapper.insert(voucher);

        BizVoucherEntry entry1 = new BizVoucherEntry();
        entry1.setTenantId(tenantId);
        entry1.setVoucherId(voucher.getId());
        entry1.setEntrySeq(1);
        entry1.setSummary("结转本年利润");
        entry1.setAccountId(profitLossAccount.getId());
        entry1.setCreateTime(Instant.now());
        BizVoucherEntry entry2 = new BizVoucherEntry();
        entry2.setTenantId(tenantId);
        entry2.setVoucherId(voucher.getId());
        entry2.setEntrySeq(2);
        entry2.setSummary("结转至未分配利润");
        entry2.setAccountId(retainedEarningsAccount.getId());
        entry2.setCreateTime(Instant.now());
        if (profitAmount.compareTo(BigDecimal.ZERO) > 0) {
            entry1.setDebitAmount(profitAmount);
            entry1.setCreditAmount(BigDecimal.ZERO);
            entry2.setDebitAmount(BigDecimal.ZERO);
            entry2.setCreditAmount(profitAmount);
        } else {
            BigDecimal lossAmount = profitAmount.abs();
            entry1.setDebitAmount(BigDecimal.ZERO);
            entry1.setCreditAmount(lossAmount);
            entry2.setDebitAmount(lossAmount);
            entry2.setCreditAmount(BigDecimal.ZERO);
        }

        voucherEntryMapper.insert(entry1);
        voucherEntryMapper.insert(entry2);

        auditLogPublisher.publish(tenantId, "period_close", "year_end_transfer", "system",
                "年度结转期间: " + periodCode + ", 本年利润: " + profitAmount + ", 凭证ID: " + voucher.getId());
        return true;
    }

    private BizAccount findRetainedEarningsAccount(String tenantId) {
        List<BizAccount> accounts = accountMapper.selectByAccountType(tenantId, "retained_earnings");
        if (accounts != null && !accounts.isEmpty()) {
            return accounts.get(0);
        }
        LambdaQueryWrapper<BizAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccount::getTenantId, tenantId)
                .eq(BizAccount::getAccountCode, "4104")
                ;
        return accountMapper.selectOne(wrapper);
    }

    private BizVoucher createAutoVoucher(String tenantId, String periodCode, String summary, BigDecimal amount) {
        BizVoucher voucher = new BizVoucher();
        voucher.setTenantId(tenantId);
        voucher.setPeriodCode(periodCode);
        voucher.setVoucherType("auto");
        voucher.setVoucherDate(Instant.now());
        voucher.setStatus(0);
        voucher.setMaker("system");
        voucher.setRemark(summary);
        voucher.setCreateTime(Instant.now());
        voucher.setUpdateTime(Instant.now());
        return voucher;
    }
}