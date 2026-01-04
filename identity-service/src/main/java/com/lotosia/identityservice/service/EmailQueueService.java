package com.lotosia.identityservice.service;

import com.lotosia.identityservice.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private static final String EMAIL_TOPIC = "email-notifications";

    public void queueOtpEmail(String recipientEmail, String otp) {
        EmailMessage message = EmailMessage.builder()
                .type("OTP")
                .recipientEmail(recipientEmail)
                .otpCode(otp)
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(EMAIL_TOPIC, recipientEmail, message);
        log.info("Queued OTP email for: {}", recipientEmail);
    }

    public void queuePasswordResetEmail(String recipientEmail, String resetLink) {
        EmailMessage message = EmailMessage.builder()
                .type("PASSWORD_RESET")
                .recipientEmail(recipientEmail)
                .resetLink(resetLink)
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(EMAIL_TOPIC, recipientEmail, message);
        log.info("Queued password reset email for: {}", recipientEmail);
    }
}
