package com.ehome.mal.finance.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.ehome.mal.finance.service.LoanEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "外部贷款事件接收")
@RestController
@RequestMapping("/external")
@RequiredArgsConstructor
public class LoanEventController {

    private final LoanEventService loanEventService;

    @ApiOperation("接收贷款事件")
    @PostMapping("/loan-event")
    public R<Long> receiveLoanEvent(@RequestBody Map<String, Object> event) {
        return R.ok(loanEventService.processLoanEvent(event));
    }
}