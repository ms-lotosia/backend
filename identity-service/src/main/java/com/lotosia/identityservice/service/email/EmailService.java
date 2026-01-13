package com.lotosia.identityservice.service.email;

import com.lotosia.identityservice.service.email.queue.EmailQueueService;
import com.lotosia.identityservice.service.email.sender.AsyncEmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final AsyncEmailSender asyncEmailSender;
    private final EmailQueueService emailQueueService;

    public void sendOtpEmailHtml(String recipientEmail, String otp) {
        try {
            emailQueueService.queueOtpEmail(recipientEmail, otp);
        } catch (Exception e) {
            sendEmailWithFallback(
                () -> asyncEmailSender.sendOtpEmailHtml(recipientEmail, otp),
                () -> {},
                () -> asyncEmailSender.sendSimpleEmail(recipientEmail, "Qeydiyyat üçün OTP Kodu",
                    "OTP kodunuz: " + otp + "\nBu kod 5 dəqiqə ərzində keçərlidir.\n\nBu kodu heç kəslə paylaşmayın.")
            );
        }
    }

    public void sendResetPasswordEmailHtml(String recipientEmail, String resetLink) {
        try {
            emailQueueService.queuePasswordResetEmail(recipientEmail, resetLink);
        } catch (Exception e) {
            sendEmailWithFallback(
                () -> asyncEmailSender.sendResetPasswordEmailHtml(recipientEmail, resetLink),
                () -> {},
                () -> asyncEmailSender.sendSimpleEmail(recipientEmail, "Reset Your Lotosia Password",
                    "Click the link to reset your password: " + resetLink + "\n\nIf you didn't request this, you can ignore this email.")
            );
        }
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
