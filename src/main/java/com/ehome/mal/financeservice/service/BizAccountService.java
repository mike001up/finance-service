package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.BizAccount;

import java.util.List;

public interface BizAccountService extends IService<BizAccount> {
    List<BizAccount> getAccountTree(String tenantId);

    boolean saveAccount(BizAccount account);

    boolean updateAccount(BizAccount account);

    boolean deleteAccount(Long id);
}
