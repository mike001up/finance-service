package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.BizFixedAsset;

import java.math.BigDecimal;

public interface BizFixedAssetService extends IService<BizFixedAsset> {
    BigDecimal calculateDepreciation(BizFixedAsset asset);

    boolean saveAsset(BizFixedAsset asset);

    boolean updateAsset(BizFixedAsset asset);
}
