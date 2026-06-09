package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizVoucherEntry;

import java.util.List;

public interface BizVoucherEntryService extends IService<BizVoucherEntry> {
    List<BizVoucherEntry> getEntriesByVoucherId(Long voucherId);

    boolean saveEntries(Long voucherId, List<BizVoucherEntry> entries);

    boolean checkBalance(List<BizVoucherEntry> entries);

    List<BizVoucherEntry> getEntriesByAccountIdAndPeriod(String tenantId, Long accountId, String periodCode);
}
