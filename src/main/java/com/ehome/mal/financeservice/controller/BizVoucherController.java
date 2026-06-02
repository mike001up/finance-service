package com.ehome.mal.financeservice.controller;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.financeservice.dto.VoucherDTO;
import com.ehome.mal.financeservice.entity.BizVoucher;
import com.ehome.mal.financeservice.service.BizVoucherService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "凭证管理")
@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class BizVoucherController {

    private final BizVoucherService voucherService;

    @ApiOperation("新增凭证")
    @PostMapping
    public R<Boolean> saveVoucher(@RequestBody BizVoucher voucher) {
        return R.ok(voucherService.saveVoucher(voucher));
    }

    @ApiOperation("审核凭证")
    @PostMapping("/review/{voucherId}")
    public R<Boolean> reviewVoucher(@PathVariable Long voucherId, @RequestParam String reviewer) {
        return R.ok(voucherService.reviewVoucher(voucherId, reviewer));
    }

    @ApiOperation("反审核凭证")
    @PostMapping("/unreview/{voucherId}")
    public R<Boolean> unreviewVoucher(@PathVariable Long voucherId) {
        return R.ok(voucherService.unreviewVoucher(voucherId));
    }

    @ApiOperation("生成凭证号")
    @GetMapping("/generateNo")
    public R<String> generateVoucherNo(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                        @RequestParam String periodCode,
                                        @RequestParam String voucherType) {
        return R.ok(voucherService.generateVoucherNo(tenantId, periodCode, voucherType));
    }

    @ApiOperation("根据ID查询凭证")
    @GetMapping("/{id}")
    public R<BizVoucher> getVoucherById(@PathVariable Long id) {
        return R.ok(voucherService.getById(id));
    }

    @ApiOperation("查询凭证列表")
    @GetMapping("/list")
    public R<List<BizVoucher>> listVouchers(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId,
                                             @RequestParam(required = false) String periodCode) {
        LambdaQueryWrapper<BizVoucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVoucher::getTenantId, tenantId);
        if (periodCode != null) {
            wrapper.eq(BizVoucher::getPeriodCode, periodCode);
        }
        wrapper.orderByDesc(BizVoucher::getVoucherNo);
        return R.ok(voucherService.list(wrapper));
    }
}
