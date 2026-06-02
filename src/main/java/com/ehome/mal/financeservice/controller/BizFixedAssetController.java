package com.ehome.mal.financeservice.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.financeservice.entity.BizFixedAsset;
import com.ehome.mal.financeservice.service.BizFixedAssetService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Api(tags = "固定资产管理")
@RestController
@RequestMapping("/fixed-asset")
@RequiredArgsConstructor
public class BizFixedAssetController {

    private final BizFixedAssetService fixedAssetService;

    @ApiOperation("新增固定资产")
    @PostMapping
    public R<Boolean> saveAsset(@RequestBody BizFixedAsset asset) {
        return R.ok(fixedAssetService.saveAsset(asset));
    }

    @ApiOperation("更新固定资产")
    @PutMapping
    public R<Boolean> updateAsset(@RequestBody BizFixedAsset asset) {
        return R.ok(fixedAssetService.updateAsset(asset));
    }

    @ApiOperation("计算折旧")
    @PostMapping("/calculateDepreciation")
    public R<BigDecimal> calculateDepreciation(@RequestBody BizFixedAsset asset) {
        return R.ok(fixedAssetService.calculateDepreciation(asset));
    }

    @ApiOperation("根据ID查询固定资产")
    @GetMapping("/{id}")
    public R<BizFixedAsset> getAssetById(@PathVariable Long id) {
        return R.ok(fixedAssetService.getById(id));
    }

    @ApiOperation("查询固定资产列表")
    @GetMapping("/list")
    public R<List<BizFixedAsset>> listAssets(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId) {
        LambdaQueryWrapper<BizFixedAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizFixedAsset::getTenantId, tenantId)
                .orderByDesc(BizFixedAsset::getCreateTime);
        return R.ok(fixedAssetService.list(wrapper));
    }
}
