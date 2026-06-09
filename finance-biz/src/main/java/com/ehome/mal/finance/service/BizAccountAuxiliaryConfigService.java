package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizAccountAuxiliaryConfig;

import java.util.List;

public interface BizAccountAuxiliaryConfigService extends IService<BizAccountAuxiliaryConfig> {
    List<BizAccountAuxiliaryConfig> listByAccountId(Long accountId);

    boolean saveConfig(BizAccountAuxiliaryConfig config);

    boolean deleteConfig(Long id);
}