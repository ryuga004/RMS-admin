package com.rms.admin.data.dto.email;

import jakarta.mail.internet.InternetAddress;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class Email {
    InternetAddress from;
    String[] to;
    String subject;
    String emailTemplate;
    Map<String,String> placeholderValues;
    Map<String, byte[]> attachments;
}