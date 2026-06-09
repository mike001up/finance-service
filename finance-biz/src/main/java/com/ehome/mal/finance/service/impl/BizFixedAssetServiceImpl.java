package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizFixedAsset;
import com.ehome.mal.finance.api.enums.DepreciationMethod;
import com.ehome.mal.finance.api.enums.FixedAssetStatus;
import com.ehome.mal.finance.mapper.BizFixedAssetMapper;
import com.ehome.mal.finance.service.BizFixedAssetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class BizFixedAssetServiceImpl extends ServiceImpl<BizFixedAssetMapper, BizFixedAsset> implements BizFixedAssetService {

    @Override
    public BigDecimal calculateDepreciation(BizFixedAsset asset) {
        BigDecimal depreciableAmount = asset.getOriginalValue().subtract(asset.getSalvageValue());
        BigDecimal depreciationAmount;

        if (DepreciationMethod.STRAIGHT_LINE.getCode().equals(asset.getDepreciationMethod())) {
            depreciationAmount = depreciableAmount.divide(
                    new BigDecimal(asset.getUsefulLife()),
                    2,
                    RoundingMode.HALF_UP
            );
        } else if (DepreciationMethod.DOUBLE_DECLINING.getCode().equals(asset.getDepreciationMethod())) {
            BigDecimal rate = new BigDecimal("2").divide(
                    new BigDecimal(asset.getUsefulLife()),
                    4,
                    RoundingMode.HALF_UP
            );
            depreciationAmount = asset.getNetValue().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } else {
            depreciationAmount = BigDecimal.ZERO;
        }

        return depreciationAmount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAsset(BizFixedAsset asset) {
        asset.setCreateTime(Instant.now());
        asset.setUpdateTime(Instant.now());
        asset.setDepreciationValue(BigDecimal.ZERO);
        asset.setNetValue(asset.getOriginalValue());
        asset.setStatus(FixedAssetStatus.IN_SERVICE.getCode());
        return save(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAsset(BizFixedAsset asset) {
        asset.setUpdateTime(Instant.now());
        return updateById(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAsset(Long id) {
        BizFixedAsset asset = getById(id);
        if (asset == null) {
            return false;
        }
        asset.setIsDel(IsDelEnum.YES);
        asset.setUpdateTime(Instant.now());
        return updateById(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disposeAsset(Long id) {
        BizFixedAsset asset = getById(id);
        if (asset == null || asset.getStatus() != FixedAssetStatus.IN_SERVICE.getCode()) {
            throw new IllegalStateException("只有在役资产才能处置");
        }
        asset.setStatus(FixedAssetStatus.DISPOSED.getCode());
        asset.setUpdateTime(Instant.now());
        return updateById(asset);
    }
}
