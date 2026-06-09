package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.SystemConfig;

public interface SystemConfigService extends IService<SystemConfig> {
    String getConfigValue(String tenantId, String configKey);

    boolean setConfigValue(String tenantId, String configKey, String configValue);
}
