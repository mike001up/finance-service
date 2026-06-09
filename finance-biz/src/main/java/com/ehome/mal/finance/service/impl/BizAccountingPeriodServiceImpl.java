package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAccountingPeriod;
import com.ehome.mal.finance.api.enums.PeriodStatus;
import com.ehome.mal.finance.mapper.BizAccountingPeriodMapper;
import com.ehome.mal.finance.service.BizAccountingPeriodService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BizAccountingPeriodServiceImpl extends ServiceImpl<BizAccountingPeriodMapper, BizAccountingPeriod> implements BizAccountingPeriodService {

    @Override
    public List<BizAccountingPeriod> getPeriodsByTenant(String tenantId) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getTenantId, tenantId)
                .orderByDesc(BizAccountingPeriod::getYear)
                .orderByDesc(BizAccountingPeriod::getMonth);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean openPeriod(String periodCode) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getPeriodCode, periodCode);
        BizAccountingPeriod period = getOne(wrapper);

        if (period == null || period.getStatus() == PeriodStatus.OPENED.getCode()) {
            return false;
        }

        period.setStatus(PeriodStatus.OPENED.getCode());
        period.setUpdateTime(Instant.now());
        return updateById(period);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePeriod(String periodCode, String closedBy) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getPeriodCode, periodCode);
        BizAccountingPeriod period = getOne(wrapper);

        if (period == null || period.getStatus() == PeriodStatus.CLOSED.getCode()) {
            return false;
        }

        period.setStatus(PeriodStatus.CLOSED.getCode());
        period.setClosedAt(Instant.now());
        period.setClosedBy(closedBy);
        period.setUpdateTime(Instant.now());
        return updateById(period);
    }

    @Override
    public BizAccountingPeriod getCurrentPeriod(String tenantId) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getTenantId, tenantId)
                .eq(BizAccountingPeriod::getStatus, PeriodStatus.OPENED.getCode())
                .orderByAsc(BizAccountingPeriod::getYear)
                .orderByAsc(BizAccountingPeriod::getMonth)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public boolean isPeriodClosed(String tenantId, String periodCode) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getTenantId, tenantId)
                .eq(BizAccountingPeriod::getPeriodCode, periodCode)
                .eq(BizAccountingPeriod::getStatus, PeriodStatus.CLOSED.getCode());
        return count(wrapper) > 0;
    }
}
