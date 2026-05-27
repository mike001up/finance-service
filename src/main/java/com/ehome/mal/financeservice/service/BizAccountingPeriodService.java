package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.BizAccountingPeriod;

import java.util.List;

public interface BizAccountingPeriodService extends IService<BizAccountingPeriod> {
    List<BizAccountingPeriod> getPeriodsByTenant(String tenantId);

    boolean openPeriod(String periodCode);

    boolean closePeriod(String periodCode);

    BizAccountingPeriod getCurrentPeriod(String tenantId);
}
