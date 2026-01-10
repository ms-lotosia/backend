package com.lotosia.identityservice.service.email.util;

import com.lotosia.identityservice.config.EmailProperties;
import com.lotosia.identityservice.service.email.model.EmailMessage;

public class EmailContentBuilder {

    private EmailContentBuilder() {
    }

    public static EmailContent buildOtpContent(String otp) {
        String plainText = "Lotosia qeydiyyatı üçün OTP kodunuz\n\n"
                + "OTP kodunuz: " + otp + "\n"
                + "Bu kod 5 dəqiqə ərzində keçərlidir.\n\n"
                + "Bu kodu heç kəslə paylaşmayın.";

        return new EmailContent("Lotosia - Qeydiyyat üçün OTP Kodu", plainText);
    }

    public static EmailContent buildPasswordResetContent(String resetLink, EmailProperties emailProperties) {
        String plainText = "Reset your Lotosia password\n\n"
                + "We received a request to reset the password for your account.\n"
                + "This link expires in 1 hour.\n\n"
                + "Reset password: " + resetLink + "\n\n"
                + "If you didn't request this, you can ignore this email or contact support at " + emailProperties.getFromAddress() + ".";

        return new EmailContent("Reset Your Lotosia Password", plainText);
    }

    public static EmailContent buildFallbackContent(EmailMessage message) {
        String subject;
        String text;

        switch (message.getType()) {
            case OTP:
                subject = "Lotosia - Qeydiyyat üçün OTP Kodu";
                text = "OTP kodunuz: " + message.getOtpCode() + "\nBu kod 5 dəqiqə ərzində keçərlidir.\n\nBu kodu heç kəslə paylaşmayın.";
                break;

            case PASSWORD_RESET:
                subject = "Reset Your Lotosia Password";
                text = "Click the link to reset your password: " + message.getResetLink() +
                      "\n\nIf you didn't request this, you can ignore this email.";
                break;

            default:
                subject = "Lotosia - Important Message";
                text = "You have a pending message. Please contact support.";
                break;
        }

        return new EmailContent(subject, text);
    }

    public static class EmailContent {
        private final String subject;
        private final String plainText;

        public EmailContent(String subject, String plainText) {
            this.subject = subject;
            this.plainText = plainText;
        }

        public String getSubject() {
            return subject;
        }

        public String getPlainText() {
            return plainText;
        }
    }
}
