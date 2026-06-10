package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenancyInvitationListItemResult {
    private Long assetId;
    private String assetTitle;
    private OffsetDateTime createdAt;
}
