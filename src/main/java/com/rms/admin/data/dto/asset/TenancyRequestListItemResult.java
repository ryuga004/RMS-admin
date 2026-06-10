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
public class TenancyRequestListItemResult {
    private Long assetId;
    private String assetTitle;
    private Long requesterUserId;
    private String requesterName;
    private OffsetDateTime createdAt;
}
