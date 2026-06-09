package com.ehome.mal.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.finance.api.entity.BizAuxiliaryItem;
import com.ehome.mal.finance.mapper.BizAuxiliaryItemMapper;
import com.ehome.mal.finance.service.BizAuxiliaryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BizAuxiliaryItemServiceImpl extends ServiceImpl<BizAuxiliaryItemMapper, BizAuxiliaryItem> implements BizAuxiliaryItemService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUXILIARY_CACHE_PREFIX = "finance:auxiliary:";
    private static final long CACHE_TTL_HOURS = 1;

    @Override
    public List<BizAuxiliaryItem> listByTenantAndType(String tenantId, String auxiliaryType) {
        String cacheKey = AUXILIARY_CACHE_PREFIX + tenantId + ":" + (auxiliaryType != null ? auxiliaryType : "all");
        @SuppressWarnings("unchecked")
        List<BizAuxiliaryItem> cached = (List<BizAuxiliaryItem>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        LambdaQueryWrapper<BizAuxiliaryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAuxiliaryItem::getTenantId, tenantId);
        if (auxiliaryType != null) {
            wrapper.eq(BizAuxiliaryItem::getAuxiliaryType, auxiliaryType);
        }
        wrapper.orderByAsc(BizAuxiliaryItem::getItemCode);
        List<BizAuxiliaryItem> result = list(wrapper);
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveItem(BizAuxiliaryItem item) {
        item.setCreateTime(Instant.now());
        item.setUpdateTime(Instant.now());
        boolean result = save(item);
        if (result) {
            evictAuxiliaryCache(item.getTenantId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateItem(BizAuxiliaryItem item) {
        item.setUpdateTime(Instant.now());
        boolean result = updateById(item);
        if (result) {
            evictAuxiliaryCache(item.getTenantId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteItem(Long id) {
        BizAuxiliaryItem item = getById(id);
        if (item == null) {
            return false;
        }
        item.setIsDel(IsDelEnum.YES);
        item.setUpdateTime(Instant.now());
        boolean result = updateById(item);
        if (result) {
            evictAuxiliaryCache(item.getTenantId());
        }
        return result;
    }

    private void evictAuxiliaryCache(String tenantId) {
        redisTemplate.delete(redisTemplate.keys(AUXILIARY_CACHE_PREFIX + tenantId + ":*"));
    }
}