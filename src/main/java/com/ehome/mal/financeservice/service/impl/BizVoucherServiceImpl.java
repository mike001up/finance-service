package com.ehome.mal.financeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ehome.mal.financeservice.entity.BizVoucher;
import com.ehome.mal.financeservice.mapper.BizVoucherMapper;
import com.ehome.mal.financeservice.service.BizVoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BizVoucherServiceImpl extends ServiceImpl<BizVoucherMapper, BizVoucher> implements BizVoucherService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveVoucher(BizVoucher voucher) {
        voucher.setCreateTime(LocalDateTime.now());
        voucher.setUpdateTime(LocalDateTime.now());
        voucher.setDelFlag(0);
        voucher.setStatus(0);
        return save(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewVoucher(Long voucherId, String reviewer) {
        BizVoucher voucher = getById(voucherId);
        if (voucher == null || voucher.getStatus() != 0) {
            return false;
        }
        voucher.setStatus(1);
        voucher.setReviewer(reviewer);
        voucher.setReviewTime(LocalDateTime.now());
        voucher.setUpdateTime(LocalDateTime.now());
        return updateById(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unreviewVoucher(Long voucherId) {
        BizVoucher voucher = getById(voucherId);
        if (voucher == null || voucher.getStatus() != 1) {
            return false;
        }
        voucher.setStatus(0);
        voucher.setReviewer(null);
        voucher.setReviewTime(null);
        voucher.setUpdateTime(LocalDateTime.now());
        return updateById(voucher);
    }

    @Override
    public String generateVoucherNo(String tenantId, String periodCode, String voucherType) {
        LambdaQueryWrapper<BizVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVoucher::getTenantId, tenantId)
                .eq(BizVoucher::getPeriodCode, periodCode)
                .eq(BizVoucher::getVoucherType, voucherType)
                .orderByDesc(BizVoucher::getVoucherNo)
                .last("LIMIT 1");
        BizVoucher lastVoucher = getOne(wrapper);
        
        int sequence = 1;
        if (lastVoucher != null) {
            String lastNo = lastVoucher.getVoucherNo();
            sequence = Integer.parseInt(lastNo.substring(lastNo.length() - 4)) + 1;
        }
        
        return String.format("%s-%s-%04d", periodCode, voucherType, sequence);
    }
}
