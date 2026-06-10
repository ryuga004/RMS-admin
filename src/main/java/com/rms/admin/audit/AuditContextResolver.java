package com.rms.admin.audit;

import com.rms.admin.security.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AuditContextResolver {

    private final AuditTypeExtractor auditTypeExtractor;
    private final ResourceIdExtractor resourceIdExtractor;

    public AuditContext resolve(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getServletPath() != null ? request.getServletPath() : request.getRequestURI();
        String method = request.getMethod();
        AuditType auditType = auditTypeExtractor.extractFromPath(path);
        String resourceId = resourceIdExtractor.extract(method, request, response);
        Long userId = getUserId();
        AuditContext.AuditAction action = mapMethodToAction(method);

        return AuditContext.builder()
                .userId(userId)
                .method(method)
                .path(path)
                .status(response.getStatus())
                .timestamp(Instant.now())
                .auditType(auditType)
                .resourceId(resourceId)
                .action(action)
                .build();
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof JwtPrincipal)) {
            return null;
        }
        return ((JwtPrincipal) auth.getPrincipal()).getUserId();
    }

    private AuditContext.AuditAction mapMethodToAction(String method) {
        if (method == null) {
            return null;
        }
        return switch (method.toUpperCase()) {
            case "POST" -> AuditContext.AuditAction.CREATE;
            case "PUT", "PATCH" -> AuditContext.AuditAction.UPDATE;
            case "DELETE" -> AuditContext.AuditAction.DELETE;
            default -> null;
        };
    }
}
