package com.lotosia.identityservice.config;

public class EmailConstants {

    private EmailConstants() {
    }

    public static final String EMAIL_TOPIC = "email-notifications";
    public static final String EMAIL_DLQ_TOPIC = "email-notifications-dlq";

    public static final int MAX_RETRY_ATTEMPTS = 5;
    public static final long MAX_MESSAGE_AGE = 24 * 60 * 60 * 1000L;

    public static final long BASE_RETRY_DELAY_MS = 1000L;
    public static final long MAX_RETRY_DELAY_MS = 5 * 60 * 1000L;
    public static final double JITTER_FACTOR = 0.25;

    public static final String OTP_SUBJECT = "Lotosia - Qeydiyyat üçün OTP Kodu";
    public static final String PASSWORD_RESET_SUBJECT = "Reset Your Lotosia Password";
    public static final String SIMPLE_EMAIL_SUBJECT_PREFIX = "Lotosia - ";

    public static final String OTP_FALLBACK_SUBJECT = "Lotosia - Qeydiyyat üçün OTP Kodu";
    public static final String PASSWORD_RESET_FALLBACK_SUBJECT = "Reset Your Lotosia Password";
}
