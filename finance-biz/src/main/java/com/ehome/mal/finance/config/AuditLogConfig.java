package com.ehome.mal.finance.config;

import com.ehome.mal.finance.event.AuditLogEvent;
import com.ehome.mal.finance.event.AuditLogListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationEventPublisher;

@Configuration
public class AuditLogConfig {

    @Bean
    public AuditLogListener auditLogListener() {
        return new AuditLogListener();
    }
}