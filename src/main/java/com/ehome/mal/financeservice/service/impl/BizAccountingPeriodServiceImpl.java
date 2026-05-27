package com.ehome.mal.financeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.financeservice.entity.BizAccountingPeriod;
import com.ehome.mal.financeservice.mapper.BizAccountingPeriodMapper;
import com.ehome.mal.financeservice.service.BizAccountingPeriodService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        
        if (period == null || period.getStatus() == 1) {
            return false;
        }
        
        period.setStatus(1);
        period.setUpdateTime(LocalDateTime.now());
        return updateById(period);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePeriod(String periodCode) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getPeriodCode, periodCode);
        BizAccountingPeriod period = getOne(wrapper);
        
        if (period == null || period.getStatus() == 2) {
            return false;
        }
        
        period.setStatus(2);
        period.setUpdateTime(LocalDateTime.now());
        return updateById(period);
    }

    @Override
    public BizAccountingPeriod getCurrentPeriod(String tenantId) {
        LambdaQueryWrapper<BizAccountingPeriod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountingPeriod::getTenantId, tenantId)
                .eq(BizAccountingPeriod::getStatus, 1)
                .orderByAsc(BizAccountingPeriod::getYear)
                .orderByAsc(BizAccountingPeriod::getMonth)
                .last("LIMIT 1");
        return getOne(wrapper);
    }
}
