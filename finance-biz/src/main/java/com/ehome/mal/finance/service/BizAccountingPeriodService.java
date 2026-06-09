package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizAccountingPeriod;

import java.util.List;

public interface BizAccountingPeriodService extends IService<BizAccountingPeriod> {
    List<BizAccountingPeriod> getPeriodsByTenant(String tenantId);

    boolean openPeriod(String periodCode);

    boolean closePeriod(String periodCode, String closedBy);

    BizAccountingPeriod getCurrentPeriod(String tenantId);

    boolean isPeriodClosed(String tenantId, String periodCode);
}
