package com.lotosia.identityservice.service.email.sender;

import com.lotosia.identityservice.service.email.config.EmailConstants;
import com.lotosia.identityservice.config.EmailProperties;
import com.lotosia.identityservice.exception.RetryableEmailException;
import com.lotosia.identityservice.service.email.sender.EmailErrorClassifier;
import com.lotosia.identityservice.service.email.util.EmailContentBuilder;
import com.lotosia.identityservice.service.email.support.EmailTemplateProcessor;
import com.lotosia.identityservice.service.email.util.EmailValidator;
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
    private final EmailErrorClassifier errorClassifier;

    public AsyncEmailSender(JavaMailSender mailSender, EmailProperties emailProperties,
                          EmailTemplateProcessor templateProcessor, EmailValidator emailValidator,
                          EmailErrorClassifier errorClassifier) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.templateProcessor = templateProcessor;
        this.emailValidator = emailValidator;
        this.errorClassifier = errorClassifier;
    }

    @Async("emailExecutor")
    public void sendPreRenderedEmail(String recipientEmail, String subject, String plainText, String htmlContent) {
        emailValidator.validateEmailAddress(recipientEmail);

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
            if (errorClassifier.isRetryable(e)) {
                throw new RetryableEmailException("Temporary email delivery failure - will retry", e);
            }
            throw new RuntimeException("Failed to send pre-rendered HTML email due to mail server error", e);
        }
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
