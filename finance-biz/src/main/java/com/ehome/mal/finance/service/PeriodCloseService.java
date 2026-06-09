package com.ehome.mal.finance.service;

import com.ehome.mal.finance.api.vo.TrialBalanceVO;

public interface PeriodCloseService {
    Long accrueInterest(String tenantId, String periodCode);

    Long depreciation(String tenantId, String periodCode);

    Long prepaidAmortization(String tenantId, String periodCode);

    Long accrualExpense(String tenantId, String periodCode);

    Long creditLoss(String tenantId, String periodCode);

    Long writeOff(String tenantId, String periodCode);

    TrialBalanceVO trialBalance(String tenantId, String periodCode);

    boolean closePeriod(String tenantId, String periodCode, String closedBy);

    boolean yearEndTransfer(String tenantId, String periodCode);
}