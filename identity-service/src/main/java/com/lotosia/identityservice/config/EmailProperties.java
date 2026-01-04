package com.lotosia.identityservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private String fromAddress = "lotosia2025@gmail.com";
    private String replyToAddress = "lotosia2025@gmail.com";
    public static final String OTP_SUBJECT = "Lotosia - Qeydiyyat üçün OTP Kodu";
    public static final String PASSWORD_RESET_SUBJECT = "Reset Your Lotosia Password";
    public static final String SIMPLE_EMAIL_SUBJECT_PREFIX = "Lotosia";
    public static final int OTP_EXPIRY_MINUTES = 5;
    public static final int PASSWORD_RESET_EXPIRY_HOURS = 1;

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getReplyToAddress() {
        return replyToAddress;
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }
}
