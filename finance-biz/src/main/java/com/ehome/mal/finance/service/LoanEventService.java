package com.ehome.mal.finance.service;

import java.util.Map;

public interface LoanEventService {
    Long processLoanEvent(Map<String, Object> event);

    Long reverseAccruedInterest(String tenantId, String periodCode, String loanNo);
}