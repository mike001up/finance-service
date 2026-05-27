package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.BizAccountBalance;

import java.util.List;

public interface BizAccountBalanceService extends IService<BizAccountBalance> {
    List<BizAccountBalance> getBalancesByPeriod(String tenantId, String periodCode);

    boolean updateBalance(Long accountId, String periodCode, java.math.BigDecimal debit, java.math.BigDecimal credit);

    boolean calculateTrialBalance(String tenantId, String periodCode);
}
