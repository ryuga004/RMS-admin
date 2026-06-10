package com.rms.admin.data.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.admin.data.dao.interfaces.IEmailTemplateDao;
import com.rms.admin.data.dto.email.EmailTemplateDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.rms.admin.persistence.tables.EmailTemplates.EMAIL_TEMPLATES;

@Repository
@RequiredArgsConstructor
public class EmailTemplateDao implements IEmailTemplateDao {

    private final DSLContext dsl;

    @Override
    public EmailTemplateDto findBySubject(String subject) {
       return dsl.select(EMAIL_TEMPLATES.SUBJECT, EMAIL_TEMPLATES.TEMPLATE, EMAIL_TEMPLATES.PLACEHOLDER_VALUES)
                .from(EMAIL_TEMPLATES)
                .where(EMAIL_TEMPLATES.SUBJECT.eq(subject))
                .fetchOneInto(EmailTemplateDto.class);
    }
}