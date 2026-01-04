package com.lotosia.identityservice.service;

import com.lotosia.identityservice.config.EmailConstants;
import com.lotosia.identityservice.config.EmailProperties;
import com.lotosia.identityservice.util.EmailContentBuilder;
import com.lotosia.identityservice.util.EmailTemplateProcessor;
import com.lotosia.identityservice.util.EmailValidator;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class AsyncEmailSender {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final EmailTemplateProcessor templateProcessor;
    private final EmailValidator emailValidator;

    public AsyncEmailSender(JavaMailSender mailSender, EmailProperties emailProperties,
                          EmailTemplateProcessor templateProcessor, EmailValidator emailValidator) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.templateProcessor = templateProcessor;
        this.emailValidator = emailValidator;
    }

    @Async("emailExecutor")
    public void sendSimpleEmail(String recipientEmail, String subject, String text) {
        emailValidator.validateEmailAddress(recipientEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(EmailConstants.SIMPLE_EMAIL_SUBJECT_PREFIX + subject);
            message.setText(text);
            message.setFrom(emailProperties.getFromAddress());
            message.setReplyTo(emailProperties.getReplyToAddress());

            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send email due to mail server error", e);
        }
    }

    @Async("emailExecutor")
    public void sendOtpEmailHtml(String recipientEmail, String otp) {
        emailValidator.validateEmailAddress(recipientEmail);
        emailValidator.validateOtpCode(otp);

        EmailContentBuilder.EmailContent content = EmailContentBuilder.buildOtpContent(otp);
        String htmlContent = templateProcessor.processOtpTemplate(otp);

        sendHtmlEmail(recipientEmail, content.getSubject(), content.getPlainText(), htmlContent);
    }

    @Async("emailExecutor")
    public void sendResetPasswordEmailHtml(String recipientEmail, String resetLink) {
        emailValidator.validateEmailAddress(recipientEmail);
        emailValidator.validateResetLink(resetLink);

        String accountEmail = recipientEmail;

        EmailContentBuilder.EmailContent content = EmailContentBuilder.buildPasswordResetContent(resetLink, emailProperties);
        String htmlContent = templateProcessor.processPasswordResetTemplate(accountEmail, resetLink);

        sendHtmlEmail(recipientEmail, content.getSubject(), content.getPlainText(), htmlContent);
    }

    private void sendHtmlEmail(String recipientEmail, String subject, String plainText, String htmlContent) {
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(recipientEmail);
            helper.setFrom(emailProperties.getFromAddress());
            helper.setReplyTo(emailProperties.getReplyToAddress());
            helper.setSubject(subject);
            helper.setText(plainText, htmlContent);
        };

        try {
            mailSender.send(preparator);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send HTML email due to mail server error", e);
        }
    }

}
