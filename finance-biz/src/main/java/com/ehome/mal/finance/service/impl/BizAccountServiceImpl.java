package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAccount;
import com.ehome.mal.finance.mapper.BizAccountMapper;
import com.ehome.mal.finance.service.BizAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BizAccountServiceImpl extends ServiceImpl<BizAccountMapper, BizAccount> implements BizAccountService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCOUNT_TREE_CACHE_PREFIX = "finance:account:tree:";
    private static final long CACHE_TTL_HOURS = 1;

    @Override
    public List<BizAccount> getAccountTree(String tenantId) {
        String cacheKey = ACCOUNT_TREE_CACHE_PREFIX + tenantId;
        @SuppressWarnings("unchecked")
        List<BizAccount> cached = (List<BizAccount>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<BizAccount> tree = baseMapper.selectTreeList(tenantId);
        redisTemplate.opsForValue().set(cacheKey, tree, CACHE_TTL_HOURS, TimeUnit.HOURS);
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAccount(BizAccount account) {
        account.setCreateTime(Instant.now());
        account.setUpdateTime(Instant.now());
        boolean result = save(account);
        if (result) {
            evictAccountTreeCache(account.getTenantId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAccount(BizAccount account) {
        account.setUpdateTime(Instant.now());
        boolean result = updateById(account);
        if (result) {
            evictAccountTreeCache(account.getTenantId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAccount(Long id) {
        BizAccount account = getById(id);
        if (account == null) {
            return false;
        }
        account.setIsDel(IsDelEnum.YES);
        account.setUpdateTime(Instant.now());
        boolean result = updateById(account);
        if (result) {
            evictAccountTreeCache(account.getTenantId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleEnable(Long id, Integer isEnabled) {
        BizAccount account = getById(id);
        if (account == null) {
            return false;
        }
        account.setIsEnabled(isEnabled);
        account.setUpdateTime(Instant.now());
        boolean result = updateById(account);
        if (result) {
            evictAccountTreeCache(account.getTenantId());
        }
        return result;
    }

    private void evictAccountTreeCache(String tenantId) {
        redisTemplate.delete(ACCOUNT_TREE_CACHE_PREFIX + tenantId);
    }
}
