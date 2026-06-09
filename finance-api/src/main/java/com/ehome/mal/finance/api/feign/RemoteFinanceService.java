package com.ehome.mal.finance.api.feign;

import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(contextId = "remoteFinanceService", value = "financeservice")
public interface RemoteFinanceService {

    @GetMapping("/account/tree")
    R<?> getAccountTree(@RequestHeader(SecurityConstants.TENANT_ID) String tenantId);
}
