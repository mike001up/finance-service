package com.ehome.mal.financeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.financeservice.entity.BizFixedAsset;
import com.ehome.mal.financeservice.mapper.BizFixedAssetMapper;
import com.ehome.mal.financeservice.service.BizFixedAssetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class BizFixedAssetServiceImpl extends ServiceImpl<BizFixedAssetMapper, BizFixedAsset> implements BizFixedAssetService {

    @Override
    public BigDecimal calculateDepreciation(BizFixedAsset asset) {
        BigDecimal depreciableAmount = asset.getOriginalValue().subtract(asset.getSalvageValue());
        BigDecimal depreciationAmount;
        
        if ("STRAIGHT_LINE".equals(asset.getDepreciationMethod())) {
            depreciationAmount = depreciableAmount.divide(
                    new BigDecimal(asset.getUsefulLife()),
                    2,
                    RoundingMode.HALF_UP
            );
        } else if ("DOUBLE_DECLINING".equals(asset.getDepreciationMethod())) {
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
        asset.setCreateTime(LocalDateTime.now());
        asset.setUpdateTime(LocalDateTime.now());
        asset.setDelFlag(0);
        asset.setDepreciationValue(BigDecimal.ZERO);
        asset.setNetValue(asset.getOriginalValue());
        return save(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAsset(BizFixedAsset asset) {
        asset.setUpdateTime(LocalDateTime.now());
        return updateById(asset);
    }
}
