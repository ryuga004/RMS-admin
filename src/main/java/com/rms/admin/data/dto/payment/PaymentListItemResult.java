package com.rms.admin.data.dto.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PaymentListItemResult {

    private Long id;
    private Long paymentOptionId;
    private String paymentOptionName;
    private Long assetId;
    private String assetTitle;
    private Long tenantUserId;
    private String tenantName;
    private String tenantEmail;
    private Long ownerUserId;
    private String ownerName;
    private String ownerEmail;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String stripeCheckoutSessionId;
    private String stripePaymentIntentId;
    private String description;
    private OffsetDateTime paidAt;
    private OffsetDateTime createdAt;
}
