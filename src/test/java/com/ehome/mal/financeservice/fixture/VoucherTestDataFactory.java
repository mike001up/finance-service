package com.ehome.mal.financeservice.fixture;

import com.ehome.mal.financeservice.entity.BizVoucher;
import com.ehome.mal.financeservice.entity.BizVoucherEntry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class VoucherTestDataFactory {

    private static final String DEFAULT_TENANT_ID = "tenant-001";
    private static final String DEFAULT_PERIOD_CODE = "202401";

    private VoucherTestDataFactory() {
    }

    public static BizVoucher createDefaultVoucher() {
        BizVoucher voucher = new BizVoucher();
        voucher.setTenantId(DEFAULT_TENANT_ID);
        voucher.setVoucherNo("202401-JZ-0001");
        voucher.setPeriodCode(DEFAULT_PERIOD_CODE);
        voucher.setVoucherType("JZ");
        voucher.setVoucherDate(LocalDateTime.of(2024, 1, 15, 0, 0));
        voucher.setVoucherCount(1);
        voucher.setAttachmentCount("0");
        voucher.setMaker("test-user");
        voucher.setStatus(0);
        voucher.setRemark("测试凭证");
        voucher.setDelFlag(0);
        return voucher;
    }

    public static BizVoucher createVoucherWithPeriod(String periodCode) {
        BizVoucher voucher = createDefaultVoucher();
        voucher.setPeriodCode(periodCode);
        return voucher;
    }

    public static BizVoucher createVoucherWithType(String voucherType) {
        BizVoucher voucher = createDefaultVoucher();
        voucher.setVoucherType(voucherType);
        return voucher;
    }

    public static BizVoucher createAuditedVoucher() {
        BizVoucher voucher = createDefaultVoucher();
        voucher.setStatus(1);
        voucher.setReviewer("auditor");
        voucher.setReviewTime(LocalDateTime.now());
        return voucher;
    }

    public static BizVoucher createPostedVoucher() {
        BizVoucher voucher = createAuditedVoucher();
        voucher.setStatus(2);
        return voucher;
    }

    public static BizVoucherEntry createDefaultEntry() {
        BizVoucherEntry entry = new BizVoucherEntry();
        entry.setAccountId(1L);
        entry.setAccountCode("1001");
        entry.setAccountName("库存现金");
        entry.setDebitAmount(new BigDecimal("100.00"));
        entry.setCreditAmount(BigDecimal.ZERO);
        entry.setDirection("DEBIT");
        entry.setRemark("借方分录");
        entry.setDelFlag(0);
        return entry;
    }

    public static BizVoucherEntry createDebitEntry(Long accountId, BigDecimal amount) {
        BizVoucherEntry entry = createDefaultEntry();
        entry.setAccountId(accountId);
        entry.setDebitAmount(amount);
        entry.setCreditAmount(BigDecimal.ZERO);
        entry.setDirection("DEBIT");
        return entry;
    }

    public static BizVoucherEntry createCreditEntry(Long accountId, BigDecimal amount) {
        BizVoucherEntry entry = createDefaultEntry();
        entry.setAccountId(accountId);
        entry.setDebitAmount(BigDecimal.ZERO);
        entry.setCreditAmount(amount);
        entry.setDirection("CREDIT");
        return entry;
    }

    public static List<BizVoucherEntry> createBalancedEntries() {
        List<BizVoucherEntry> entries = new ArrayList<>();
        BigDecimal amount = new BigDecimal("1000.00");
        
        entries.add(createDebitEntry(1L, amount));
        entries.add(createCreditEntry(2L, amount));
        
        return entries;
    }

    public static List<BizVoucherEntry> createUnbalancedEntries() {
        List<BizVoucherEntry> entries = new ArrayList<>();
        
        entries.add(createDebitEntry(1L, new BigDecimal("1000.00")));
        entries.add(createCreditEntry(2L, new BigDecimal("900.00")));
        
        return entries;
    }

    public static List<BizVoucherEntry> createMultiEntries() {
        List<BizVoucherEntry> entries = new ArrayList<>();
        
        entries.add(createDebitEntry(1L, new BigDecimal("500.00")));
        entries.add(createDebitEntry(2L, new BigDecimal("500.00")));
        entries.add(createCreditEntry(3L, new BigDecimal("1000.00")));
        
        return entries;
    }

    public static String getDefaultTenantId() {
        return DEFAULT_TENANT_ID;
    }

    public static String getDefaultPeriodCode() {
        return DEFAULT_PERIOD_CODE;
    }
}
