package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizAuxiliaryItem;
import com.ehome.mal.finance.service.BizAuxiliaryItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "辅助核算档案管理")
@RestController
@RequestMapping("/auxiliary-item")
@RequiredArgsConstructor
public class BizAuxiliaryItemController {

    private final BizAuxiliaryItemService auxiliaryItemService;

    @ApiOperation("查询辅助核算项列表")
    @GetMapping("/list")
    public R<List<BizAuxiliaryItem>> list(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                          @RequestParam(required = false) String auxiliaryType) {
        return R.ok(auxiliaryItemService.listByTenantAndType(tenantId, auxiliaryType));
    }

    @ApiOperation("新增辅助核算项")
    @PostMapping
    public R<Boolean> save(@RequestBody BizAuxiliaryItem item) {
        return R.ok(auxiliaryItemService.saveItem(item));
    }

    @ApiOperation("更新辅助核算项")
    @PutMapping
    public R<Boolean> update(@RequestBody BizAuxiliaryItem item) {
        return R.ok(auxiliaryItemService.updateItem(item));
    }

    @ApiOperation("删除辅助核算项")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(auxiliaryItemService.deleteItem(id));
    }

    @ApiOperation("根据ID查询辅助核算项")
    @GetMapping("/{id}")
    public R<BizAuxiliaryItem> getById(@PathVariable Long id) {
        return R.ok(auxiliaryItemService.getById(id));
    }
}