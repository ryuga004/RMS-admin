package com.rms.admin.service.messagePublisher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeDetails {
    private String name;
    private String routingKey;
    @Builder.Default
    private String type = "topic";
}