package com.rms.admin.data.dto.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PaymentOptionResponse {

    private Long id;
    private Long assetId;
    private String assetTitle;
    private Long ownerId;
    private String name;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private boolean isRecurring;
    private String recurringInterval;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
