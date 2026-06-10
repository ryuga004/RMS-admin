package com.rms.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    @NotBlank
    private String secretKey;

    @NotBlank
    private String webhookSecret;

    private String currency = "usd";
}
