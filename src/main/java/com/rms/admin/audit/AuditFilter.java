package com.rms.admin.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private final AuditContextResolver auditContextResolver;
    private final AuditMessageFactory auditMessageFactory;
    private final AuditPublisher auditPublisher;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (path != null && (path.startsWith("/register") || path.startsWith("/actuator"))) {
            return true;
        }
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);

        AuditContext context = auditContextResolver.resolve(request, response);
        if (context.getAuditType() == AuditType.UNKNOWN) {
            return;
        }

        try {
            AuditEvent event = auditMessageFactory.create(context);
            auditPublisher.publish(event);
        } catch (Exception e) {
            log.error("Audit publish failed: path={}, method={}", request.getRequestURI(), request.getMethod(), e);
        }
    }
}
