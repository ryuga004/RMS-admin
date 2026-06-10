package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetListItemResult {
    private Long id;
    private String title;
    private BigDecimal rent;
    private Long capacity;
    private String categoryName;
    private String imageKey;
    private String imageUrl;
}