package com.rms.admin.data.dto.asset;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenancyInvitationRequest {
    @NotNull(message = "tenantUserId is required")
    private Long tenantUserId;
}
