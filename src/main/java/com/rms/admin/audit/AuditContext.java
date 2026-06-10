package com.rms.admin.audit;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AuditContext {
    Long userId;
    String method;
    String path;
    int status;
    Instant timestamp;
    AuditType auditType;
    String resourceId;
    AuditAction action;

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE
    }
}
