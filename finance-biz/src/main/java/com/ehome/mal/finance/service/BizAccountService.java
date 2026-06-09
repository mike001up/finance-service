package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizAccount;

import java.util.List;

public interface BizAccountService extends IService<BizAccount> {
    List<BizAccount> getAccountTree(String tenantId);

    boolean saveAccount(BizAccount account);

    boolean updateAccount(BizAccount account);

    boolean deleteAccount(Long id);

    boolean toggleEnable(Long id, Integer isEnabled);
}
