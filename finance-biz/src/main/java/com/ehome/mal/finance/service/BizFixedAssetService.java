package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizFixedAsset;

import java.math.BigDecimal;

public interface BizFixedAssetService extends IService<BizFixedAsset> {
    BigDecimal calculateDepreciation(BizFixedAsset asset);

    boolean saveAsset(BizFixedAsset asset);

    boolean updateAsset(BizFixedAsset asset);

    boolean deleteAsset(Long id);

    boolean disposeAsset(Long id);
}
