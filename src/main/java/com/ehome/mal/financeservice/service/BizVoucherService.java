package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.BizVoucher;

public interface BizVoucherService extends IService<BizVoucher> {
    boolean saveVoucher(BizVoucher voucher);

    boolean reviewVoucher(Long voucherId, String reviewer);

    boolean unreviewVoucher(Long voucherId);

    String generateVoucherNo(String tenantId, String periodCode, String voucherType);
}
