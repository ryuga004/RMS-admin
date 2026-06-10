package com.rms.admin.data.dto.asset;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class CreateAssetRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long categoryId;

    @Min(0)
    private Long capacity;

    @DecimalMin("0")
    private BigDecimal rent;

    private List<String> tags;

    @NotNull(message = "Address details are required")
    @Valid
    private AddressDetails addressDetails;
}
