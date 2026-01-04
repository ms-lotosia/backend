package com.lotosia.identityservice.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final AsyncEmailSender asyncEmailSender;

    public EmailService(AsyncEmailSender asyncEmailSender) {
        this.asyncEmailSender = asyncEmailSender;
    }

    public void sendOtpEmailHtml(String recipientEmail, String otp) {
        try {
            asyncEmailSender.sendOtpEmailHtml(recipientEmail, otp);
        } catch (Exception e) {
            asyncEmailSender.sendSimpleEmail(recipientEmail, "Qeydiyyat üçün OTP Kodu",
                "OTP kodunuz: " + otp + "\nBu kod 5 dəqiqə ərzində keçərlidir.\n\nBu kodu heç kəslə paylaşmayın.");
        }
    }


    public void sendResetPasswordEmailHtml(String recipientEmail, String resetLink) {
        try {
            asyncEmailSender.sendResetPasswordEmailHtml(recipientEmail, resetLink);
        } catch (Exception e) {
            asyncEmailSender.sendSimpleEmail(recipientEmail, "Reset Your Lotosia Password",
                "Click the link to reset your password: " + resetLink + "\n\nIf you didn't request this, you can ignore this email.");
        }
    }
}
