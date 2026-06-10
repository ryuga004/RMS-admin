package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private Long capacity;
    private BigDecimal rent;
    private List<String> tags;
    private OffsetDateTime createdAt;
    private List<String> imageKeys;
    private List<String> imageUrls;
    private AddressDetails addressDetails;
}
