package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizVoucher;

public interface BizVoucherService extends IService<BizVoucher> {
    boolean saveVoucher(BizVoucher voucher);

    boolean updateVoucher(BizVoucher voucher);

    boolean deleteVoucher(Long id);

    boolean reviewVoucher(Long voucherId, String reviewer);

    boolean unreviewVoucher(Long voucherId);

    String generateVoucherNo(String tenantId, String periodCode, String voucherType);
}
