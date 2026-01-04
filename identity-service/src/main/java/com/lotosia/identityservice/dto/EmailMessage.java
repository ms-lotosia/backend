package com.lotosia.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {
    private String type; // "OTP" or "PASSWORD_RESET"
    private String recipientEmail;
    private String otpCode; // for OTP emails
    private String resetLink; // for password reset emails
    private long timestamp;
    private int retryCount; // for retry logic
}
