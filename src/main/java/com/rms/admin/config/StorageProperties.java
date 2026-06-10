package com.rms.admin.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String endpoint = "";
    @NotBlank
    private String accessKey;
    @NotBlank
    private String secretKey;
    @NotBlank
    private String bucket;
    private String region = "us-east-1";
    private boolean useSsl = false;
    private int presignedExpiryMinutes = 5;
}