package com.ehome.mal.financeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.financeservice.entity.BizVoucherEntry;

import java.util.List;

public interface BizVoucherEntryService extends IService<BizVoucherEntry> {
    List<BizVoucherEntry> getEntriesByVoucherId(Long voucherId);

    boolean saveEntries(Long voucherId, List<BizVoucherEntry> entries);

    boolean checkBalance(List<BizVoucherEntry> entries);
}
