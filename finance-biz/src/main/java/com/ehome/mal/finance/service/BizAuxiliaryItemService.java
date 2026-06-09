package com.ehome.mal.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ehome.mal.finance.api.entity.BizAuxiliaryItem;

import java.util.List;

public interface BizAuxiliaryItemService extends IService<BizAuxiliaryItem> {
    List<BizAuxiliaryItem> listByTenantAndType(String tenantId, String auxiliaryType);

    boolean saveItem(BizAuxiliaryItem item);

    boolean updateItem(BizAuxiliaryItem item);

    boolean deleteItem(Long id);
}