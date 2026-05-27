package com.ehome.mal.financeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.financeservice.entity.BizAccount;
import com.ehome.mal.financeservice.mapper.BizAccountMapper;
import com.ehome.mal.financeservice.service.BizAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BizAccountServiceImpl extends ServiceImpl<BizAccountMapper, BizAccount> implements BizAccountService {

    @Override
    public List<BizAccount> getAccountTree(String tenantId) {
        return baseMapper.selectTreeList(tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAccount(BizAccount account) {
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());
        account.setDelFlag(0);
        return save(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAccount(BizAccount account) {
        account.setUpdateTime(LocalDateTime.now());
        return updateById(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAccount(Long id) {
        BizAccount account = getById(id);
        if (account == null) {
            return false;
        }
        account.setDelFlag(1);
        account.setUpdateTime(LocalDateTime.now());
        return updateById(account);
    }
}
