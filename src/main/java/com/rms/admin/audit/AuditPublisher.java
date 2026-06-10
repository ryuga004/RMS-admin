package com.rms.admin.audit;

public interface AuditPublisher {

    void publish(AuditEvent event);
}
