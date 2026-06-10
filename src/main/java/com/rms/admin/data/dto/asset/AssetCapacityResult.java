package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetCapacityResult {
    private Long capacity;
    private Long currentTenantCount;
}
