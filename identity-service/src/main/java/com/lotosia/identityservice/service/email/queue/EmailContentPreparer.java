package com.lotosia.identityservice.service.email.queue;

import com.lotosia.identityservice.config.EmailProperties;
import com.lotosia.identityservice.service.email.model.EmailMessage;
import com.lotosia.identityservice.service.email.util.EmailContentBuilder;
import com.lotosia.identityservice.service.email.support.EmailTemplateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailContentPreparer {

    private final EmailTemplateProcessor templateProcessor;
    private final EmailProperties emailProperties;

    public EmailMessage prepareOtpEmail(String recipientEmail, String otp) {
        EmailContentBuilder.EmailContent content = EmailContentBuilder.buildOtpContent(otp);
        String htmlContent = templateProcessor.processOtpTemplate(otp);

        return EmailMessage.builder()
                .type(EmailMessage.EmailType.OTP)
                .recipientEmail(recipientEmail)
                .otpCode(otp)
                .subject(content.getSubject())
                .plainText(content.getPlainText())
                .htmlContent(htmlContent)
                .timestamp(System.currentTimeMillis())
                .retryCount(0)
                .build();
    }

    public EmailMessage preparePasswordResetEmail(String recipientEmail, String resetLink) {
        EmailContentBuilder.EmailContent content = EmailContentBuilder.buildPasswordResetContent(resetLink, emailProperties);
        String htmlContent = templateProcessor.processPasswordResetTemplate(recipientEmail, resetLink);

        return EmailMessage.builder()
                .type(EmailMessage.EmailType.PASSWORD_RESET)
                .recipientEmail(recipientEmail)
                .resetLink(resetLink)
                .subject(content.getSubject())
                .plainText(content.getPlainText())
                .htmlContent(htmlContent)
                .timestamp(System.currentTimeMillis())
                .retryCount(0)
                .build();
    }
}