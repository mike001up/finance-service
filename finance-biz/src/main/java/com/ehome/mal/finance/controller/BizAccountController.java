package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizAccount;
import com.ehome.mal.finance.service.BizAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "科目管理")
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class BizAccountController {

    private final BizAccountService accountService;

    @ApiOperation("获取科目树")
    @GetMapping("/tree")
    public R<List<BizAccount>> getAccountTree(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId) {
        return R.ok(accountService.getAccountTree(tenantId));
    }

    @ApiOperation("新增科目")
    @PostMapping
    public R<Boolean> saveAccount(@RequestBody BizAccount account) {
        return R.ok(accountService.saveAccount(account));
    }

    @ApiOperation("更新科目")
    @PutMapping
    public R<Boolean> updateAccount(@RequestBody BizAccount account) {
        return R.ok(accountService.updateAccount(account));
    }

    @ApiOperation("删除科目")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteAccount(@PathVariable Long id) {
        return R.ok(accountService.deleteAccount(id));
    }

    @ApiOperation("根据ID查询科目")
    @GetMapping("/{id}")
    public R<BizAccount> getAccountById(@PathVariable Long id) {
        return R.ok(accountService.getById(id));
    }

    @ApiOperation("启用/禁用科目")
    @PutMapping("/{id}/toggle-enable")
    public R<Boolean> toggleEnable(@PathVariable Long id, @RequestParam Integer isEnabled) {
        return R.ok(accountService.toggleEnable(id, isEnabled));
    }
}
