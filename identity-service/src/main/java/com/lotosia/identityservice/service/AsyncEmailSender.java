package com.lotosia.identityservice.service;

import com.lotosia.identityservice.config.EmailProperties;
import jakarta.mail.MessagingException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;

@Service
public class AsyncEmailSender {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    public AsyncEmailSender(JavaMailSender mailSender, EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
    }

    @Async("emailExecutor")
    public void sendSimpleEmail(String recipientEmail, String subject, String text) {
        validateEmailAddress(recipientEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(EmailProperties.SIMPLE_EMAIL_SUBJECT_PREFIX + " - " + subject);
            message.setText(text);
            message.setFrom(emailProperties.getFromAddress());
            message.setReplyTo(emailProperties.getReplyToAddress());

            mailSender.send(message);
        } catch (MailException e) {
        }
    }

    @Async("emailExecutor")
    public void sendOtpEmailHtml(String recipientEmail, String otp) {
        validateEmailAddress(recipientEmail);
        validateOtpCode(otp);

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true, // multipart mode for HTML
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(recipientEmail);
            helper.setFrom(emailProperties.getFromAddress());
            helper.setReplyTo(emailProperties.getReplyToAddress());
            helper.setSubject(EmailProperties.OTP_SUBJECT);

            String plainText = "Lotosia qeydiyyatı üçün OTP kodunuz\n\n"
                    + "OTP kodunuz: " + otp + "\n"
                    + "Bu kod 5 dəqiqə ərzində keçərlidir.\n\n"
                    + "Bu kodu heç kəslə paylaşmayın.";

            String htmlTemplate = loadTemplate("templates/otp-email.html");
            String html = htmlTemplate
                    .replace("{{OTP_CODE}}", StringEscapeUtils.escapeHtml4(otp))
                    .replace("{{CURRENT_YEAR}}", String.valueOf(Year.now().getValue()));

            helper.setText(plainText, html);
        };

        try {
            mailSender.send(preparator);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send OTP email due to mail server error", e);
        }
    }

    @Async("emailExecutor")
    public void sendResetPasswordEmailHtml(String recipientEmail, String resetLink) {
        validateEmailAddress(recipientEmail);
        validateResetLink(resetLink);

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true, // multipart mode for HTML
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(recipientEmail);
            helper.setFrom(emailProperties.getFromAddress());
            helper.setReplyTo(emailProperties.getReplyToAddress());
            helper.setSubject(EmailProperties.PASSWORD_RESET_SUBJECT);

            String accountEmail = recipientEmail;

            String plainText = "Reset your Lotosia password\n\n"
                    + "We received a request to reset the password for: " + accountEmail + "\n"
                    + "This link expires in 1 hour.\n\n"
                    + "Reset password: " + resetLink + "\n\n"
                    + "If you didn't request this, you can ignore this email or contact support at " + emailProperties.getFromAddress() + ".";

            String htmlTemplate = loadTemplate("templates/password-reset-email.html");
            String html = htmlTemplate
                    .replace("{{ACCOUNT_EMAIL}}", StringEscapeUtils.escapeHtml4(accountEmail))
                    .replace("{{RESET_LINK}}", StringEscapeUtils.escapeHtml4(resetLink))
                    .replace("{{CURRENT_YEAR}}", String.valueOf(Year.now().getValue()));

            helper.setText(plainText, html);
        };

        try {
            mailSender.send(preparator);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send password reset email due to mail server error", e);
        }
    }

    private String loadTemplate(String templatePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private void validateEmailAddress(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (!pattern.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
    }

    private void validateOtpCode(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP code cannot be null or empty");
        }

        if (otp.length() != 6) {
            throw new IllegalArgumentException("OTP code must be 6 digits");
        }

        if (!otp.matches("\\d{6}")) {
            throw new IllegalArgumentException("OTP code must contain only digits");
        }
    }

    private void validateResetLink(String resetLink) {
        if (resetLink == null || resetLink.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset link cannot be null or empty");
        }

        if (!resetLink.startsWith("http://")) {
            throw new IllegalArgumentException("Reset link must use HTTP protocol");
        }

        if (resetLink.contains("..") || resetLink.contains("://localhost")) {
            throw new IllegalArgumentException("Reset link contains potentially unsafe content");
        }
    }
}
