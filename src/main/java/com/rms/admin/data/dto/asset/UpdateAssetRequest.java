package com.rms.admin.data.dto.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssetRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    private Long capacity;
    
    @NotNull(message = "Rent is required")
    private BigDecimal rent;
    
    private List<String> tags;
    
    @NotNull(message = "Address details are required")
    private AddressDetails addressDetails;
}
