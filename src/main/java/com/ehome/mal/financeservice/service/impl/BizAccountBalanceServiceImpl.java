package com.ehome.mal.financeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.financeservice.entity.BizAccountBalance;
import com.ehome.mal.financeservice.mapper.BizAccountBalanceMapper;
import com.ehome.mal.financeservice.service.BizAccountBalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BizAccountBalanceServiceImpl extends ServiceImpl<BizAccountBalanceMapper, BizAccountBalance> implements BizAccountBalanceService {

    @Override
    public List<BizAccountBalance> getBalancesByPeriod(String tenantId, String periodCode) {
        LambdaQueryWrapper<BizAccountBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountBalance::getTenantId, tenantId)
                .eq(BizAccountBalance::getPeriodCode, periodCode);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBalance(Long accountId, String periodCode, BigDecimal debit, BigDecimal credit) {
        LambdaQueryWrapper<BizAccountBalance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountBalance::getAccountId, accountId)
                .eq(BizAccountBalance::getPeriodCode, periodCode);
        BizAccountBalance balance = getOne(wrapper);
        
        if (balance == null) {
            balance = new BizAccountBalance();
            balance.setAccountId(accountId);
            balance.setPeriodCode(periodCode);
            balance.setPeriodDebit(debit != null ? debit : BigDecimal.ZERO);
            balance.setPeriodCredit(credit != null ? credit : BigDecimal.ZERO);
            balance.setYearDebit(debit != null ? debit : BigDecimal.ZERO);
            balance.setYearCredit(credit != null ? credit : BigDecimal.ZERO);
            balance.setEndingBalance(BigDecimal.ZERO);
            balance.setCreateTime(LocalDateTime.now());
            balance.setDelFlag(0);
            return save(balance);
        } else {
            if (debit != null) {
                balance.setPeriodDebit(balance.getPeriodDebit().add(debit));
                balance.setYearDebit(balance.getYearDebit().add(debit));
            }
            if (credit != null) {
                balance.setPeriodCredit(balance.getPeriodCredit().add(credit));
                balance.setYearCredit(balance.getYearCredit().add(credit));
            }
            balance.setUpdateTime(LocalDateTime.now());
            return updateById(balance);
        }
    }

    @Override
    public boolean calculateTrialBalance(String tenantId, String periodCode) {
        List<BizAccountBalance> balances = getBalancesByPeriod(tenantId, periodCode);
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (BizAccountBalance balance : balances) {
            totalDebit = totalDebit.add(balance.getYearDebit());
            totalCredit = totalCredit.add(balance.getYearCredit());
        }
        
        return totalDebit.compareTo(totalCredit) == 0;
    }
}
