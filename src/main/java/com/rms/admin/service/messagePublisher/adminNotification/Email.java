package com.rms.admin.service.messagePublisher.adminNotification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    private String to;
    private String from;
    private String subject;
    private String body;
    private boolean html = false;
}
