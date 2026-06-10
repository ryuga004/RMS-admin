package com.rms.admin.data.dao.interfaces;

import com.rms.admin.data.dto.email.EmailTemplateDto;

public interface IEmailTemplateDao {
    EmailTemplateDto findBySubject(String subject);
}