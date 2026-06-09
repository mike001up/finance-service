package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAccountingPeriod;
import com.ehome.mal.finance.api.entity.BizVoucher;
import com.ehome.mal.finance.api.entity.BizVoucherEntry;
import com.ehome.mal.finance.api.enums.PeriodStatus;
import com.ehome.mal.finance.api.enums.VoucherStatus;
import com.ehome.mal.finance.mapper.BizVoucherMapper;
import com.ehome.mal.finance.service.BizAccountBalanceService;
import com.ehome.mal.finance.service.BizAccountingPeriodService;
import com.ehome.mal.finance.service.BizVoucherEntryService;
import com.ehome.mal.finance.service.BizVoucherService;
import com.ehome.mal.finance.event.AuditLogPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BizVoucherServiceImpl extends ServiceImpl<BizVoucherMapper, BizVoucher> implements BizVoucherService {

    private final BizVoucherEntryService voucherEntryService;
    private final BizAccountBalanceService accountBalanceService;
    private final BizAccountingPeriodService accountingPeriodService;
    private final AuditLogPublisher auditLogPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveVoucher(BizVoucher voucher) {
        voucher.setCreateTime(Instant.now());
        voucher.setUpdateTime(Instant.now());
        voucher.setStatus(VoucherStatus.DRAFT.getCode());
        return save(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVoucher(BizVoucher voucher) {
        BizVoucher existing = getById(voucher.getId());
        if (existing == null || existing.getStatus() != VoucherStatus.DRAFT.getCode()) {
            throw new IllegalStateException("只有草稿状态的凭证才能修改");
        }
        BizAccountingPeriod period = accountingPeriodService.getCurrentPeriod(existing.getTenantId());
        if (period != null && period.getPeriodCode().equals(existing.getPeriodCode())
                && period.getStatus() == PeriodStatus.CLOSED.getCode()) {
            throw new IllegalStateException("已关账期间的凭证不能修改");
        }
        voucher.setUpdateTime(Instant.now());
        return updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVoucher(Long id) {
        BizVoucher voucher = getById(id);
        if (voucher == null || voucher.getStatus() != VoucherStatus.DRAFT.getCode()) {
            throw new IllegalStateException("只有草稿状态的凭证才能删除");
        }
        BizAccountingPeriod period = accountingPeriodService.getCurrentPeriod(voucher.getTenantId());
        if (period != null && period.getPeriodCode().equals(voucher.getPeriodCode())
                && period.getStatus() == PeriodStatus.CLOSED.getCode()) {
            throw new IllegalStateException("已关账期间的凭证不能删除");
        }
        voucher.setIsDel(IsDelEnum.YES);
        voucher.setUpdateTime(Instant.now());
        return updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewVoucher(Long voucherId, String reviewer) {
        BizVoucher voucher = getById(voucherId);
        if (voucher == null || voucher.getStatus() != VoucherStatus.DRAFT.getCode()) {
            throw new IllegalStateException("只有草稿状态的凭证才能审核");
        }
        voucher.setStatus(VoucherStatus.AUDITED.getCode());
        voucher.setReviewer(reviewer);
        voucher.setReviewTime(Instant.now());
        voucher.setUpdateTime(Instant.now());
        boolean updated = updateById(voucher);

        if (updated) {
            List<BizVoucherEntry> entries = voucherEntryService.getEntriesByVoucherId(voucherId);
            for (BizVoucherEntry entry : entries) {
                BigDecimal debit = entry.getDebitAmount() != null ? entry.getDebitAmount() : BigDecimal.ZERO;
                BigDecimal credit = entry.getCreditAmount() != null ? entry.getCreditAmount() : BigDecimal.ZERO;
                accountBalanceService.updateBalance(entry.getAccountId(), voucher.getPeriodCode(), debit, credit);
            }
            auditLogPublisher.publish(voucher.getTenantId(), "voucher", "review", reviewer, "审核凭证ID: " + voucherId);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unreviewVoucher(Long voucherId) {
        BizVoucher voucher = getById(voucherId);
        if (voucher == null || voucher.getStatus() != VoucherStatus.AUDITED.getCode()) {
            throw new IllegalStateException("只有已审核状态的凭证才能反审核");
        }

        BizAccountingPeriod period = accountingPeriodService.getCurrentPeriod(voucher.getTenantId());
        if (period != null && period.getPeriodCode().equals(voucher.getPeriodCode())
                && period.getStatus() == PeriodStatus.CLOSED.getCode()) {
            throw new IllegalStateException("已关账期间的凭证不能反审核");
        }

        voucher.setStatus(VoucherStatus.DRAFT.getCode());
        voucher.setReviewer(null);
        voucher.setReviewTime(null);
        voucher.setUpdateTime(Instant.now());
        boolean updated = updateById(voucher);

        if (updated) {
            List<BizVoucherEntry> entries = voucherEntryService.getEntriesByVoucherId(voucherId);
            for (BizVoucherEntry entry : entries) {
                BigDecimal debit = entry.getDebitAmount() != null ? entry.getDebitAmount().negate() : BigDecimal.ZERO;
                BigDecimal credit = entry.getCreditAmount() != null ? entry.getCreditAmount().negate() : BigDecimal.ZERO;
                accountBalanceService.updateBalance(entry.getAccountId(), voucher.getPeriodCode(), debit, credit);
            }
            auditLogPublisher.publish(voucher.getTenantId(), "voucher", "unreview", "system", "反审核凭证ID: " + voucherId);
        }
        return updated;
    }

    @Override
    public String generateVoucherNo(String tenantId, String periodCode, String voucherType) {
        LambdaQueryWrapper<BizVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVoucher::getTenantId, tenantId)
                .eq(BizVoucher::getPeriodCode, periodCode)
                .eq(BizVoucher::getVoucherType, voucherType)
                .orderByDesc(BizVoucher::getVoucherNo)
                .last("LIMIT 1");
        BizVoucher lastVoucher = getOne(wrapper);

        int sequence = 1;
        if (lastVoucher != null) {
            String lastNo = lastVoucher.getVoucherNo();
            sequence = Integer.parseInt(lastNo.substring(lastNo.length() - 4)) + 1;
        }

        return String.format("%s-%s-%04d", periodCode, voucherType, sequence);
    }
}
