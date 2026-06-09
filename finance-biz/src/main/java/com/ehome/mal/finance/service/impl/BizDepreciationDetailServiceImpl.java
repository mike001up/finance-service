package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizDepreciationDetail;
import com.ehome.mal.finance.mapper.BizDepreciationDetailMapper;
import com.ehome.mal.finance.service.BizDepreciationDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BizDepreciationDetailServiceImpl extends ServiceImpl<BizDepreciationDetailMapper, BizDepreciationDetail> implements BizDepreciationDetailService {

    @Override
    public List<BizDepreciationDetail> listByPeriod(String tenantId, String periodCode) {
        LambdaQueryWrapper<BizDepreciationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDepreciationDetail::getTenantId, tenantId)
                .eq(BizDepreciationDetail::getPeriodCode, periodCode)
                .orderByAsc(BizDepreciationDetail::getAssetId);
        return list(wrapper);
    }

    @Override
    public List<BizDepreciationDetail> listByAssetId(Long assetId) {
        LambdaQueryWrapper<BizDepreciationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDepreciationDetail::getAssetId, assetId)
                .orderByAsc(BizDepreciationDetail::getPeriodCode);
        return list(wrapper);
    }
}