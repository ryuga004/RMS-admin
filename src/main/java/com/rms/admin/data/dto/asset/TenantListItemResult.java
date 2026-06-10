package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantListItemResult {
    private Long tenantUserId;
    private String tenantName;
    private String tenantEmail;
    private Long assetId;
    private String assetTitle;
    private OffsetDateTime assignedAt;
}
