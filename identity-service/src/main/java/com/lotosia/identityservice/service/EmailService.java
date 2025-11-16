package com.lotosia.identityservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author: nijataghayev
 */

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    private EmailService self;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    public void setSelf(@Lazy EmailService self) {
        this.self = self;
    }

    @Async
    public void sendSimpleEmail(String recipientEmail, String subject, String text) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromEmailAddress);

            mailSender.send(message);

        } catch (MailException e) {

        } catch (Exception e) {

        }
    }

    public void sendOtpEmail(String recipientEmail, String otp) {
        String subject = "Sayt Qeydiyyatı üçün OTP Kodu";
        String text = "Sizin qeydiyyat üçün OTP kodunuz: " + otp + "\n" +
                "Bu kod 5 dəqiqə ərzində keçərlidir.";

        self.sendSimpleEmail(recipientEmail, subject, text);
    }
}
