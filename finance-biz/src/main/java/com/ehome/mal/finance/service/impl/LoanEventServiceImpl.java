package com.ehome.mal.finance.service.impl;

import com.ehome.mal.finance.api.entity.BizVoucher;
import com.ehome.mal.finance.api.entity.BizVoucherEntry;
import com.ehome.mal.finance.mapper.BizVoucherEntryMapper;
import com.ehome.mal.finance.mapper.BizVoucherMapper;
import com.ehome.mal.finance.service.LoanEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanEventServiceImpl implements LoanEventService {

    private final BizVoucherMapper voucherMapper;
    private final BizVoucherEntryMapper voucherEntryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long processLoanEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String tenantId = (String) event.get("tenantId");
        String periodCode = (String) event.get("periodCode");
        String loanNo = (String) event.get("loanNo");
        BigDecimal amount = toBigDecimal(event.get("amount"));

        log.info("Processing loan event: type={}, tenantId={}, loanNo={}, amount={}", eventType, tenantId, loanNo, amount);

        if ("repayment".equals(eventType)) {
            return processRepayment(event, tenantId, periodCode, loanNo, amount);
        }

        if ("overdue".equals(eventType)) {
            return processOverdue(event, tenantId, periodCode, loanNo, amount);
        }

        Long debitAccountId = toLong(event.get("debitAccountId"));
        Long creditAccountId = toLong(event.get("creditAccountId"));
        String remark = buildRemark(eventType, loanNo);
        BizVoucher voucher = createVoucher(tenantId, periodCode, remark);
        voucherMapper.insert(voucher);

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && debitAccountId != null && creditAccountId != null) {
            insertEntryPair(voucher.getId(), tenantId, remark, debitAccountId, creditAccountId, amount);
        } else {
            log.warn("Loan event missing amount/debitAccountId/creditAccountId, voucher created without entries: loanNo={}", loanNo);
        }

        return voucher.getId();
    }

    private Long processRepayment(Map<String, Object> event, String tenantId, String periodCode, String loanNo, BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Repayment amount is zero or null for loanNo={}", loanNo);
            return null;
        }

        BigDecimal receivableInterest = toBigDecimal(event.getOrDefault("receivableInterest", BigDecimal.ZERO));
        BigDecimal interestIncome = toBigDecimal(event.getOrDefault("interestIncome", BigDecimal.ZERO));
        BigDecimal principal = toBigDecimal(event.getOrDefault("principal", BigDecimal.ZERO));

        Long cashAccountId = toLong(event.get("cashAccountId"));
        Long receivableInterestAccountId = toLong(event.get("receivableInterestAccountId"));
        Long interestIncomeAccountId = toLong(event.get("interestIncomeAccountId"));
        Long loanAccountId = toLong(event.get("loanAccountId"));

        if (receivableInterest == null) receivableInterest = BigDecimal.ZERO;
        if (interestIncome == null) interestIncome = BigDecimal.ZERO;
        if (principal == null) principal = BigDecimal.ZERO;

        BigDecimal specifiedTotal = receivableInterest.add(interestIncome).add(principal);
        if (specifiedTotal.compareTo(BigDecimal.ZERO) == 0) {
            receivableInterest = totalAmount;
            specifiedTotal = totalAmount;
        }

        BigDecimal remaining = totalAmount;
        List<BizVoucherEntry> entries = new ArrayList<>();
        int seq = 1;

        BigDecimal actualReceivableInterest = receivableInterest.min(remaining);
        if (actualReceivableInterest.compareTo(BigDecimal.ZERO) > 0 && receivableInterestAccountId != null) {
            remaining = remaining.subtract(actualReceivableInterest);
            BizVoucherEntry entry = new BizVoucherEntry();
            entry.setTenantId(tenantId);
            entry.setEntrySeq(seq++);
            entry.setSummary("还款-冲减应收利息-" + loanNo);
            entry.setAccountId(receivableInterestAccountId);
            entry.setDebitAmount(BigDecimal.ZERO);
            entry.setCreditAmount(actualReceivableInterest);
            entry.setCreateTime(Instant.now());
            entries.add(entry);
        }

        BigDecimal actualInterestIncome = interestIncome.min(remaining);
        if (actualInterestIncome.compareTo(BigDecimal.ZERO) > 0 && interestIncomeAccountId != null) {
            remaining = remaining.subtract(actualInterestIncome);
            BizVoucherEntry entry = new BizVoucherEntry();
            entry.setTenantId(tenantId);
            entry.setEntrySeq(seq++);
            entry.setSummary("还款-冲减利息收入-" + loanNo);
            entry.setAccountId(interestIncomeAccountId);
            entry.setDebitAmount(BigDecimal.ZERO);
            entry.setCreditAmount(actualInterestIncome);
            entry.setCreateTime(Instant.now());
            entries.add(entry);
        }

        BigDecimal actualPrincipal = principal.min(remaining);
        if (actualPrincipal.compareTo(BigDecimal.ZERO) > 0 && loanAccountId != null) {
            remaining = remaining.subtract(actualPrincipal);
            BizVoucherEntry entry = new BizVoucherEntry();
            entry.setTenantId(tenantId);
            entry.setEntrySeq(seq++);
            entry.setSummary("还款-偿还本金-" + loanNo);
            entry.setAccountId(loanAccountId);
            entry.setDebitAmount(BigDecimal.ZERO);
            entry.setCreditAmount(actualPrincipal);
            entry.setCreateTime(Instant.now());
            entries.add(entry);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0 && loanAccountId != null) {
            BizVoucherEntry entry = new BizVoucherEntry();
            entry.setTenantId(tenantId);
            entry.setEntrySeq(seq++);
            entry.setSummary("还款-剩余偿还本金-" + loanNo);
            entry.setAccountId(loanAccountId);
            entry.setDebitAmount(BigDecimal.ZERO);
            entry.setCreditAmount(remaining);
            entry.setCreateTime(Instant.now());
            entries.add(entry);
            remaining = BigDecimal.ZERO;
        }

        if (cashAccountId != null && !entries.isEmpty()) {
            BizVoucherEntry cashEntry = new BizVoucherEntry();
            cashEntry.setTenantId(tenantId);
            cashEntry.setEntrySeq(seq);
            cashEntry.setSummary("还款-现金/银行-" + loanNo);
            cashEntry.setAccountId(cashAccountId);
            cashEntry.setDebitAmount(totalAmount.subtract(remaining));
            cashEntry.setCreditAmount(BigDecimal.ZERO);
            cashEntry.setCreateTime(Instant.now());
            entries.add(cashEntry);
        }

        BizVoucher voucher = createVoucher(tenantId, periodCode, "还款冲减-" + loanNo);
        voucherMapper.insert(voucher);

        for (BizVoucherEntry entry : entries) {
            entry.setVoucherId(voucher.getId());
            voucherEntryMapper.insert(entry);
        }

        log.info("Repayment processed: loanNo={}, total={}, receivableInterest={}, interestIncome={}, principal={}, voucherId={}",
                loanNo, totalAmount, actualReceivableInterest, actualInterestIncome, actualPrincipal, voucher.getId());
        return voucher.getId();
    }

    private Long processOverdue(Map<String, Object> event, String tenantId, String periodCode, String loanNo, BigDecimal amount) {
        Long debitAccountId = toLong(event.get("debitAccountId"));
        Long creditAccountId = toLong(event.get("creditAccountId"));

        String remark = "逾期入账-" + loanNo;
        BizVoucher voucher = createVoucher(tenantId, periodCode, remark);
        voucherMapper.insert(voucher);

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && debitAccountId != null && creditAccountId != null) {
            insertEntryPair(voucher.getId(), tenantId, remark, debitAccountId, creditAccountId, amount);
        }

        Integer overdueDays = toInteger(event.get("overdueDays"));
        if (overdueDays != null && overdueDays >= 90) {
            log.info("Loan {} overdue >= 90 days ({}), eligible for interest suspension and reversal", loanNo, overdueDays);
        }

        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reverseAccruedInterest(String tenantId, String periodCode, String loanNo) {
        Long receivableInterestAccountId = null;
        Long interestIncomeAccountId = null;

        List<BizVoucherEntry> accruedEntries = voucherEntryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizVoucherEntry>()
                        .eq(BizVoucherEntry::getTenantId, tenantId)
                        .likeRight(BizVoucherEntry::getSummary, "计提利息-" + loanNo)
        );

        if (accruedEntries.isEmpty()) {
            log.warn("No accrued interest entries found for loanNo={} to reverse", loanNo);
            return null;
        }

        BigDecimal totalToReverse = BigDecimal.ZERO;
        for (BizVoucherEntry entry : accruedEntries) {
            if (entry.getCreditAmount() != null && entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalToReverse = totalToReverse.add(entry.getCreditAmount());
                if (interestIncomeAccountId == null) interestIncomeAccountId = entry.getAccountId();
            }
            if (entry.getDebitAmount() != null && entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (receivableInterestAccountId == null) receivableInterestAccountId = entry.getAccountId();
            }
        }

        if (totalToReverse.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        String remark = "逾期停息冲回-" + loanNo;
        BizVoucher voucher = createVoucher(tenantId, periodCode, remark);
        voucherMapper.insert(voucher);

        int seq = 1;
        if (interestIncomeAccountId != null) {
            BizVoucherEntry debitEntry = new BizVoucherEntry();
            debitEntry.setTenantId(tenantId);
            debitEntry.setVoucherId(voucher.getId());
            debitEntry.setEntrySeq(seq++);
            debitEntry.setSummary(remark + "-冲回利息收入");
            debitEntry.setAccountId(interestIncomeAccountId);
            debitEntry.setDebitAmount(totalToReverse);
            debitEntry.setCreditAmount(BigDecimal.ZERO);
            debitEntry.setCreateTime(Instant.now());
            voucherEntryMapper.insert(debitEntry);
        }

        if (receivableInterestAccountId != null) {
            BizVoucherEntry creditEntry = new BizVoucherEntry();
            creditEntry.setTenantId(tenantId);
            creditEntry.setVoucherId(voucher.getId());
            creditEntry.setEntrySeq(seq);
            creditEntry.setSummary(remark + "-冲回应收利息");
            creditEntry.setAccountId(receivableInterestAccountId);
            creditEntry.setDebitAmount(BigDecimal.ZERO);
            creditEntry.setCreditAmount(totalToReverse);
            creditEntry.setCreateTime(Instant.now());
            voucherEntryMapper.insert(creditEntry);
        }

        log.info("Reversed accrued interest for loanNo={}, amount={}, voucherId={}", loanNo, totalToReverse, voucher.getId());
        return voucher.getId();
    }

    private void insertEntryPair(Long voucherId, String tenantId, String remark,
                                  Long debitAccountId, Long creditAccountId, BigDecimal amount) {
        BizVoucherEntry debitEntry = new BizVoucherEntry();
        debitEntry.setTenantId(tenantId);
        debitEntry.setVoucherId(voucherId);
        debitEntry.setEntrySeq(1);
        debitEntry.setSummary(remark);
        debitEntry.setAccountId(debitAccountId);
        debitEntry.setDebitAmount(amount);
        debitEntry.setCreditAmount(BigDecimal.ZERO);
        debitEntry.setCreateTime(Instant.now());
        voucherEntryMapper.insert(debitEntry);

        BizVoucherEntry creditEntry = new BizVoucherEntry();
        creditEntry.setTenantId(tenantId);
        creditEntry.setVoucherId(voucherId);
        creditEntry.setEntrySeq(2);
        creditEntry.setSummary(remark);
        creditEntry.setAccountId(creditAccountId);
        creditEntry.setDebitAmount(BigDecimal.ZERO);
        creditEntry.setCreditAmount(amount);
        creditEntry.setCreateTime(Instant.now());
        voucherEntryMapper.insert(creditEntry);
    }

    private String buildRemark(String eventType, String loanNo) {
        return switch (eventType) {
            case "disbursement" -> "放款-" + loanNo;
            case "repayment" -> "还款-" + loanNo;
            case "overdue" -> "逾期入账-" + loanNo;
            case "interest_accrual" -> "计提利息-" + loanNo;
            default -> "贷款事件-" + eventType + "-" + loanNo;
        };
    }

    private BizVoucher createVoucher(String tenantId, String periodCode, String remark) {
        BizVoucher voucher = new BizVoucher();
        voucher.setTenantId(tenantId);
        voucher.setPeriodCode(periodCode);
        voucher.setVoucherType("auto");
        voucher.setVoucherDate(Instant.now());
        voucher.setStatus(0);
        voucher.setMaker("system");
        voucher.setRemark(remark);
        voucher.setCreateTime(Instant.now());
        voucher.setUpdateTime(Instant.now());
        return voucher;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}