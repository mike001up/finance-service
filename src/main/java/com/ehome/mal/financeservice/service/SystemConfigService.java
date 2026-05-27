package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    String getConfigValue(String tenantId, String configKey);

    boolean setConfigValue(String tenantId, String configKey, String configValue);
}
