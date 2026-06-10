package com.rms.admin.audit;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultAuditMessageFactory implements AuditMessageFactory {

    @Override
    public AuditEvent create(AuditContext context) {
        return AuditEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(context.getAuditType())
                .userId(context.getUserId())
                .method(context.getMethod())
                .path(context.getPath())
                .resourceId(context.getResourceId())
                .action(context.getAction())
                .status(context.getStatus())
                .timestamp(context.getTimestamp())
                .build();
    }
}
