package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.api.entity.BizDepreciationDetail;
import com.ehome.mal.finance.service.BizDepreciationDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "折旧明细查询")
@RestController
@RequestMapping("/depreciation-detail")
@RequiredArgsConstructor
public class BizDepreciationDetailController {

    private final BizDepreciationDetailService depreciationDetailService;

    @ApiOperation("按期间查询折旧明细")
    @GetMapping("/list")
    public R<List<BizDepreciationDetail>> listByPeriod(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                                        @RequestParam String periodCode) {
        return R.ok(depreciationDetailService.listByPeriod(tenantId, periodCode));
    }

    @ApiOperation("按资产查询折旧明细")
    @GetMapping("/asset/{assetId}")
    public R<List<BizDepreciationDetail>> listByAssetId(@PathVariable Long assetId) {
        return R.ok(depreciationDetailService.listByAssetId(assetId));
    }
}