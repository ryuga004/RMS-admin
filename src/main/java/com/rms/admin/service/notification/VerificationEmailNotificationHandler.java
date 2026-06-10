package com.rms.admin.service.notification;

import com.rms.admin.data.dto.email.EmailTemplateDto;
import com.rms.admin.handler.EmailTemplateHandler;
import com.rms.admin.service.messagePublisher.adminNotification.AdminNotificationPublisher;
import com.rms.admin.service.messagePublisher.adminNotification.Email;
import com.rms.admin.utils.constants.EmailTemplateConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationEmailNotificationHandler {

    private final EmailTemplateHandler emailTemplateHandler;
    private final AdminNotificationPublisher adminNotificationPublisher;

    @Value("${app.email.super-admin:noreply@example.com}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        EmailTemplateDto template = emailTemplateHandler.getVerificationTemplate();
        String body = fillPlaceholders(template.getTemplate(), toEmail, verificationCode);
        String subject = template.getSubject();
        Email message = Email.builder()
                .to(toEmail)
                .from(fromEmail)
                .subject(subject)
                .body(body)
                .build();
        adminNotificationPublisher.publishEmail(message);
    }

    private String fillPlaceholders(String template, String email, String verificationCode) {
        return template
                .replace("{{" + EmailTemplateConstants.PLACEHOLDER_EMAIL + "}}", email)
                .replace("{{" + EmailTemplateConstants.PLACEHOLDER_VERIFICATION_CODE + "}}", verificationCode);
    }
}
