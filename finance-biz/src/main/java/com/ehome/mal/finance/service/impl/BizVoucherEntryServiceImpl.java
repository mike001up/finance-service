package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizVoucher;
import com.ehome.mal.finance.api.entity.BizVoucherEntry;
import com.ehome.mal.finance.mapper.BizVoucherEntryMapper;
import com.ehome.mal.finance.service.BizVoucherEntryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class BizVoucherEntryServiceImpl extends ServiceImpl<BizVoucherEntryMapper, BizVoucherEntry> implements BizVoucherEntryService {

    @Override
    public List<BizVoucherEntry> getEntriesByVoucherId(Long voucherId) {
        LambdaQueryWrapper<BizVoucherEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVoucherEntry::getVoucherId, voucherId)
                .orderByAsc(BizVoucherEntry::getEntrySeq);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveEntries(Long voucherId, List<BizVoucherEntry> entries) {
        LambdaQueryWrapper<BizVoucherEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVoucherEntry::getVoucherId, voucherId);
        remove(wrapper);

        for (int i = 0; i < entries.size(); i++) {
            BizVoucherEntry entry = entries.get(i);
            entry.setVoucherId(voucherId);
            entry.setEntrySeq(i + 1);
            entry.setCreateTime(Instant.now());
            entry.setUpdateTime(Instant.now());
            }
        return saveBatch(entries);
    }

    @Override
    public boolean checkBalance(List<BizVoucherEntry> entries) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (BizVoucherEntry entry : entries) {
            if (entry.getDebitAmount() != null) {
                totalDebit = totalDebit.add(entry.getDebitAmount());
            }
            if (entry.getCreditAmount() != null) {
                totalCredit = totalCredit.add(entry.getCreditAmount());
            }
        }

        return totalDebit.compareTo(totalCredit) == 0;
    }

    @Override
    public List<BizVoucherEntry> getEntriesByAccountIdAndPeriod(String tenantId, Long accountId, String periodCode) {
        LambdaQueryWrapper<BizVoucherEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVoucherEntry::getTenantId, tenantId)
                .eq(BizVoucherEntry::getAccountId, accountId)
                .orderByAsc(BizVoucherEntry::getVoucherId)
                .orderByAsc(BizVoucherEntry::getEntrySeq);
        return list(wrapper);
    }
}
