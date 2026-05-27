package com.ehome.mal.financeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.financeservice.entity.SystemConfig;
import com.ehome.mal.financeservice.mapper.SystemConfigMapper;
import com.ehome.mal.financeservice.service.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
            config.setCreateTime(LocalDateTime.now());
            config.setDelFlag(0);
            return save(config);
        } else {
            config.setConfigValue(configValue);
            config.setUpdateTime(LocalDateTime.now());
            return updateById(config);
        }
    }
}
