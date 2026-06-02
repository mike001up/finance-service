package com.ehome.mal.financeservice.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.financeservice.entity.SystemConfig;
import com.ehome.mal.financeservice.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "系统配置管理")
@RestController
@RequestMapping("/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @ApiOperation("获取配置值")
    @GetMapping("/value")
    public R<String> getConfigValue(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                     @RequestParam String configKey) {
        return R.ok(systemConfigService.getConfigValue(tenantId, configKey));
    }

    @ApiOperation("设置配置值")
    @PostMapping("/value")
    public R<Boolean> setConfigValue(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                      @RequestParam String configKey,
                                      @RequestParam String configValue) {
        return R.ok(systemConfigService.setConfigValue(tenantId, configKey, configValue));
    }
}
