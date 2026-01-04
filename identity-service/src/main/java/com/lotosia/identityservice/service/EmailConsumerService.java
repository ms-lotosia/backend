package com.lotosia.identityservice.service;

import com.lotosia.identityservice.config.EmailConstants;
import com.lotosia.identityservice.dto.EmailMessage;
import com.lotosia.identityservice.util.EmailContentBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailConsumerService {

    private final AsyncEmailSender asyncEmailSender;
    private final EmailQueueService emailQueueService;

    @KafkaListener(
        topics = EmailConstants.EMAIL_TOPIC,
        groupId = "email-service-group",
        containerFactory = "emailKafkaListenerContainerFactory"
    )
    public void processEmail(
            @Payload EmailMessage message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.PARTITION) int partition,
            Acknowledgment acknowledgment) {

        try {
            if (message.isExpired(EmailConstants.MAX_MESSAGE_AGE)) {
                acknowledgment.acknowledge();
                return;
            }

            boolean success = processEmailByType(message);

            if (success) {
                acknowledgment.acknowledge();
            } else {
                handleProcessingFailure(message, acknowledgment);
            }

        } catch (Exception e) {
            handleProcessingFailure(message, acknowledgment);
        }
    }

    @KafkaListener(
        topics = EmailConstants.EMAIL_DLQ_TOPIC,
        groupId = "email-dlq-group",
        containerFactory = "emailKafkaListenerContainerFactory"
    )
    public void processDeadLetterQueue(
            @Payload EmailMessage message,
            Acknowledgment acknowledgment) {

        try {
            sendFallbackEmail(message);
        } catch (Exception e) {
        }

        acknowledgment.acknowledge();
    }

    private boolean processEmailByType(EmailMessage message) {
        try {
            switch (message.getType()) {
                case OTP:
                    asyncEmailSender.sendOtpEmailHtml(message.getRecipientEmail(), message.getOtpCode());
                    return true;

                case PASSWORD_RESET:
                    asyncEmailSender.sendResetPasswordEmailHtml(message.getRecipientEmail(), message.getResetLink());
                    return true;

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    
    private void handleProcessingFailure(EmailMessage message, Acknowledgment acknowledgment) {
        EmailMessage retryMessage = message.incrementRetryCount();

        if (retryMessage.hasExceededMaxRetries(EmailConstants.MAX_RETRY_ATTEMPTS)) {
        } else {
            emailQueueService.queueEmailForRetry(retryMessage);
        }

        acknowledgment.acknowledge();
    }

    private void sendFallbackEmail(EmailMessage message) {
        EmailContentBuilder.EmailContent content = EmailContentBuilder.buildFallbackContent(message);
        asyncEmailSender.sendSimpleEmail(message.getRecipientEmail(), content.getSubject(), content.getPlainText());
    }
}
