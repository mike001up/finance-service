package com.ehome.mal.finance.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public class AuditLogListener {

    @Async
    @EventListener
    public void onAuditLog(AuditLogEvent event) {
        log.info("[AUDIT] tenantId={}, module={}, action={}, operator={}, detail={}, time={}",
                event.getTenantId(), event.getModule(), event.getAction(),
                event.getOperator(), event.getDetail(), event.getEventTime());
    }
}