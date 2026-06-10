package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenancyInvitationResponse {
    private Long assetId;
    private String assetTitle;
    private OffsetDateTime createdAt;
}
