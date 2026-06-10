package com.rms.admin.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEvent {
    private String eventId;
    private AuditType type;
    private Long userId;
    private String method;
    private String path;
    private String resourceId;
    private AuditContext.AuditAction action;
    private int status;
    private Instant timestamp;
}
