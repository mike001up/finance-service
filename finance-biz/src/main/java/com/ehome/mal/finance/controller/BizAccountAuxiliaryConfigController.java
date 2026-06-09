package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizAccountAuxiliaryConfig;
import com.ehome.mal.finance.service.BizAccountAuxiliaryConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "科目辅助核算配置管理")
@RestController
@RequestMapping("/account-auxiliary-config")
@RequiredArgsConstructor
public class BizAccountAuxiliaryConfigController {

    private final BizAccountAuxiliaryConfigService auxiliaryConfigService;

    @ApiOperation("查询科目辅助核算配置")
    @GetMapping("/list/{accountId}")
    public R<List<BizAccountAuxiliaryConfig>> listByAccountId(@PathVariable Long accountId) {
        return R.ok(auxiliaryConfigService.listByAccountId(accountId));
    }

    @ApiOperation("新增科目辅助核算配置")
    @PostMapping
    public R<Boolean> save(@RequestBody BizAccountAuxiliaryConfig config) {
        return R.ok(auxiliaryConfigService.saveConfig(config));
    }

    @ApiOperation("删除科目辅助核算配置")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(auxiliaryConfigService.deleteConfig(id));
    }
}