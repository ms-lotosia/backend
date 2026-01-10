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
    private String subject;
    private String plainText;
    private String htmlContent;
    private long timestamp;
    private int retryCount;

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