package com.lotosia.identityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailRetryService {

    private final AsyncEmailSender asyncEmailSender;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Retryable(
        value = {Exception.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendOtpEmailWithRetry(String recipientEmail, String otp) {
        asyncEmailSender.sendOtpEmailHtml(recipientEmail, otp);
    }

    @Retryable(
        value = {Exception.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendPasswordResetEmailWithRetry(String recipientEmail, String resetLink) {
        asyncEmailSender.sendResetPasswordEmailHtml(recipientEmail, resetLink);
    }

    @Recover
    public void recoverOtpEmailFailure(Exception e, String recipientEmail, String otp) {
        try {
            asyncEmailSender.sendSimpleEmail(recipientEmail, "Qeydiyyat üçün OTP Kodu",
                "OTP kodunuz: " + otp + "\nBu kod 5 dəqiqə ərzində keçərlidir.\n\nBu kodu heç kəslə paylaşmayın.");
        } catch (Exception fallbackException) {
        }
    }

    @Recover
    public void recoverPasswordResetEmailFailure(Exception e, String recipientEmail, String resetLink) {
        try {
            asyncEmailSender.sendSimpleEmail(recipientEmail, "Reset Your Lotosia Password",
                "Click the link to reset your password: " + resetLink +
                "\n\nIf you didn't request this, you can ignore this email.");
        } catch (Exception fallbackException) {
        }
    }
}
