package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizDepreciationDetail;

import java.util.List;

public interface BizDepreciationDetailService extends IService<BizDepreciationDetail> {
    List<BizDepreciationDetail> listByPeriod(String tenantId, String periodCode);

    List<BizDepreciationDetail> listByAssetId(Long assetId);
}