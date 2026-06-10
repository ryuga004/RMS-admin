package com.rms.admin.handler;

import com.rms.admin.data.dao.interfaces.IEmailTemplateDao;
import com.rms.admin.data.dto.email.EmailTemplateDto;
import com.rms.admin.utils.constants.EmailTemplateConstants;
import com.rms.admin.exception.ServiceUnavailableException;
import com.rms.admin.utils.constants.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailTemplateHandler {

    private final IEmailTemplateDao emailTemplateDao;

    public EmailTemplateDto getVerificationTemplate() {
        EmailTemplateDto dto = emailTemplateDao.findBySubject(EmailTemplateConstants.EMAIL_VERIFICATION_SUBJECT);
        if (dto == null) {
            throw new ServiceUnavailableException(Messages.ERROR_TEMPLATE_NOT_FOUND, "Email verification template not found");
        }
        return dto;
    }

    public EmailTemplateDto getPaymentReceivedOwnerTemplate() {
        EmailTemplateDto dto = emailTemplateDao.findBySubject(EmailTemplateConstants.PAYMENT_RECEIVED_OWNER_SUBJECT);
        if (dto == null) {
            throw new ServiceUnavailableException(Messages.ERROR_TEMPLATE_NOT_FOUND, "Payment received owner email template not found");
        }
        return dto;
    }

    public EmailTemplateDto getPaymentReceiptTenantTemplate() {
        EmailTemplateDto dto = emailTemplateDao.findBySubject(EmailTemplateConstants.PAYMENT_RECEIPT_TENANT_SUBJECT);
        if (dto == null) {
            throw new ServiceUnavailableException(Messages.ERROR_TEMPLATE_NOT_FOUND, "Payment receipt tenant email template not found");
        }
        return dto;
    }

    /**
     * Replaces {{placeholder}} tokens in the template with actual values.
     */
    public static String fillTemplate(String template, java.util.Map<String, String> values) {
        if (template == null) return "";
        String result = template;
        for (java.util.Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}
