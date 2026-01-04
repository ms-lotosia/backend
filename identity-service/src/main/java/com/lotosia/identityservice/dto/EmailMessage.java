package com.lotosia.identityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    public enum EmailType {
        OTP,
        PASSWORD_RESET
    }

    private EmailType type;
    private String recipientEmail;
    private String otpCode;
    private String resetLink;
    private long timestamp;
    private int retryCount;

    public static EmailMessage otpEmail(String recipientEmail, String otpCode) {
        return EmailMessage.builder()
                .type(EmailType.OTP)
                .recipientEmail(recipientEmail)
                .otpCode(otpCode)
                .timestamp(System.currentTimeMillis())
                .retryCount(0)
                .build();
    }

    public static EmailMessage passwordResetEmail(String recipientEmail, String resetLink) {
        return EmailMessage.builder()
                .type(EmailType.PASSWORD_RESET)
                .recipientEmail(recipientEmail)
                .resetLink(resetLink)
                .timestamp(System.currentTimeMillis())
                .retryCount(0)
                .build();
    }

    public boolean isExpired(long maxAgeMillis) {
        return System.currentTimeMillis() - timestamp > maxAgeMillis;
    }

    public EmailMessage incrementRetryCount() {
        this.retryCount++;
        return this;
    }

    public boolean hasExceededMaxRetries(int maxRetries) {
        return retryCount >= maxRetries;
    }
}