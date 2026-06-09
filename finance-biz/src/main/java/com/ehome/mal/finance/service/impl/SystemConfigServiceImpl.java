package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.SystemConfig;
import com.ehome.mal.finance.mapper.SystemConfigMapper;
import com.ehome.mal.finance.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getConfigValue(String tenantId, String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getTenantId, tenantId)
                .eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setConfigValue(String tenantId, String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getTenantId, tenantId)
                .eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = getOne(wrapper);
        
        if (config == null) {
            config = new SystemConfig();
            config.setTenantId(tenantId);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setCreateTime(Instant.now());
            return save(config);
        } else {
            config.setConfigValue(configValue);
            config.setUpdateTime(Instant.now());
            return updateById(config);
        }
    }
}
