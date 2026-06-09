package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAccountAuxiliaryConfig;
import com.ehome.mal.finance.mapper.BizAccountAuxiliaryConfigMapper;
import com.ehome.mal.finance.service.BizAccountAuxiliaryConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BizAccountAuxiliaryConfigServiceImpl extends ServiceImpl<BizAccountAuxiliaryConfigMapper, BizAccountAuxiliaryConfig> implements BizAccountAuxiliaryConfigService {

    @Override
    public List<BizAccountAuxiliaryConfig> listByAccountId(Long accountId) {
        LambdaQueryWrapper<BizAccountAuxiliaryConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAccountAuxiliaryConfig::getAccountId, accountId);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(BizAccountAuxiliaryConfig config) {
        config.setCreateTime(Instant.now());
        config.setUpdateTime(Instant.now());
        return save(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long id) {
        BizAccountAuxiliaryConfig config = getById(id);
        if (config == null) {
            return false;
        }
        config.setIsDel(IsDelEnum.YES);
        config.setUpdateTime(Instant.now());
        return updateById(config);
    }
}