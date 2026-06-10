package com.rms.admin.audit;

import org.springframework.stereotype.Component;

@Component
public class AuditTypeExtractor {

    /**
     * Extracts audit type from request path. More specific path patterns are checked first.
     */
    public AuditType extractFromPath(String path) {
        if (path == null || path.isBlank()) {
            return AuditType.UNKNOWN;
        }
        if (path.contains("/tenancy-invitations")) {
            return AuditType.TENANCY_INVITATION;
        }
        if (path.contains("/tenancy-requests")) {
            return AuditType.TENANCY_REQUEST;
        }
        if (path.startsWith("/users")) {
            return AuditType.USER;
        }
        if (path.startsWith("/assets")) {
            return AuditType.ASSET;
        }
        if (path.startsWith("/categories")) {
            return AuditType.CATEGORY;
        }
        return AuditType.UNKNOWN;
    }
}
