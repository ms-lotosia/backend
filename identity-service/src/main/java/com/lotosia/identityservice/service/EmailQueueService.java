package com.lotosia.identityservice.service;

import com.lotosia.identityservice.service.email.config.EmailConstants;
import com.lotosia.identityservice.service.email.model.EmailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private final EmailContentPreparer emailContentPreparer;

    public void queueOtpEmail(String recipientEmail, String otp) {
        EmailMessage message = emailContentPreparer.prepareOtpEmail(recipientEmail, otp);
        kafkaTemplate.send(EmailConstants.EMAIL_TOPIC, message.getRecipientEmail(), message);
    }

    public void queuePasswordResetEmail(String recipientEmail, String resetLink) {
        EmailMessage message = emailContentPreparer.preparePasswordResetEmail(recipientEmail, resetLink);
        kafkaTemplate.send(EmailConstants.EMAIL_TOPIC, message.getRecipientEmail(), message);
    }

    public void queueEmailForRetry(EmailMessage message) {
        EmailMessage retryMessage = message.incrementRetryCount();

        if (retryMessage.hasExceededMaxRetries(EmailConstants.MAX_RETRY_ATTEMPTS)) {
            kafkaTemplate.send(EmailConstants.EMAIL_DLQ_TOPIC, message.getRecipientEmail(), retryMessage);
        } else {
            kafkaTemplate.send(EmailConstants.EMAIL_TOPIC, message.getRecipientEmail(), retryMessage);
        }
    }

    public String getQueueStatus() {
        return "Email queue is operational";
    }
}