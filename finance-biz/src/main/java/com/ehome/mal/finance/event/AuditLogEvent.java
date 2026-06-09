package com.ehome.mal.finance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class AuditLogEvent extends ApplicationEvent {

    private final String tenantId;
    private final String module;
    private final String action;
    private final String operator;
    private final String detail;
    private final Instant eventTime;

    public AuditLogEvent(Object source, String tenantId, String module, String action, String operator, String detail) {
        super(source);
        this.tenantId = tenantId;
        this.module = module;
        this.action = action;
        this.operator = operator;
        this.detail = detail;
        this.eventTime = Instant.now();
    }
}