package com.ehome.mal.finance.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(String tenantId, String module, String action, String operator, String detail) {
        publisher.publishEvent(new AuditLogEvent(this, tenantId, module, action, operator, detail));
    }
}