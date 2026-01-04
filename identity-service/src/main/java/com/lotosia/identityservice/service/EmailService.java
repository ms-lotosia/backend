package com.lotosia.identityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final AsyncEmailSender asyncEmailSender;
    private final EmailQueueService emailQueueService;

    public void sendOtpEmailHtml(String recipientEmail, String otp) {
        sendEmailWithFallback(
            () -> asyncEmailSender.sendOtpEmailHtml(recipientEmail, otp),
            () -> emailQueueService.queueOtpEmail(recipientEmail, otp),
            () -> asyncEmailSender.sendSimpleEmail(recipientEmail, "Qeydiyyat üçün OTP Kodu",
                "OTP kodunuz: " + otp + "\nBu kod 5 dəqiqə ərzində keçərlidir.\n\nBu kodu heç kəslə paylaşmayın.")
        );
    }

    public void sendResetPasswordEmailHtml(String recipientEmail, String resetLink) {
        sendEmailWithFallback(
            () -> asyncEmailSender.sendResetPasswordEmailHtml(recipientEmail, resetLink),
            () -> emailQueueService.queuePasswordResetEmail(recipientEmail, resetLink),
            () -> asyncEmailSender.sendSimpleEmail(recipientEmail, "Reset Your Lotosia Password",
                "Click the link to reset your password: " + resetLink + "\n\nIf you didn't request this, you can ignore this email.")
        );
    }

    private void sendEmailWithFallback(Runnable primarySender, Runnable queueSender, Runnable fallbackSender) {
        try {
            primarySender.run();
        } catch (Exception e) {
            try {
                queueSender.run();
            } catch (Exception queueException) {
                try {
                    fallbackSender.run();
                } catch (Exception fallbackException) {
                }
            }
        }
    }

    public String getEmailQueueStatus() {
        return emailQueueService.getQueueStatus();
    }
}
