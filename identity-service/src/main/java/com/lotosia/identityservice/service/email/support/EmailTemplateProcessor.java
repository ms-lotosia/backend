package com.lotosia.identityservice.service.email.support;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailTemplateProcessor {

    private volatile String otpEmailTemplate;
    private volatile String passwordResetEmailTemplate;

    public String processOtpTemplate(String otpCode) {
        String template = loadOtpTemplate();
        Map<String, String> variables = Map.of(
            "OTP_CODE", StringEscapeUtils.escapeHtml4(otpCode),
            "CURRENT_YEAR", String.valueOf(Year.now().getValue())
        );
        return replaceVariables(template, variables);
    }

    public String processPasswordResetTemplate(String accountEmail, String resetLink) {
        String template = loadPasswordResetTemplate();
        Map<String, String> variables = Map.of(
            "ACCOUNT_EMAIL", StringEscapeUtils.escapeHtml4(accountEmail),
            "RESET_LINK", StringEscapeUtils.escapeHtml4(resetLink),
            "CURRENT_YEAR", String.valueOf(Year.now().getValue())
        );
        return replaceVariables(template, variables);
    }


    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private String loadOtpTemplate() {
        if (otpEmailTemplate == null) {
            synchronized (this) {
                if (otpEmailTemplate == null) {
                    otpEmailTemplate = loadTemplate("templates/otp-email.html");
                }
            }
        }
        return otpEmailTemplate;
    }

    private String loadPasswordResetTemplate() {
        if (passwordResetEmailTemplate == null) {
            synchronized (this) {
                if (passwordResetEmailTemplate == null) {
                    passwordResetEmailTemplate = loadTemplate("templates/password-reset-email.html");
                }
            }
        }
        return passwordResetEmailTemplate;
    }

    private String loadTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load email template: " + templatePath, e);
        }
    }

    public void clearCache() {
        synchronized (this) {
            otpEmailTemplate = null;
            passwordResetEmailTemplate = null;
        }
    }
}