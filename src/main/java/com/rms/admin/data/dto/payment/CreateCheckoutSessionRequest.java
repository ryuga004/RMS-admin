package com.rms.admin.data.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {

    @NotNull(message = "Payment option ID is required")
    private Long paymentOptionId;
}
