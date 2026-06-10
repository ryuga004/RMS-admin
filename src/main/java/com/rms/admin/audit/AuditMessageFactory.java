package com.rms.admin.audit;

public interface AuditMessageFactory {

    AuditEvent create(AuditContext context);
}
