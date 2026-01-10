package com.lotosia.identityservice.messaging.kafka.email;

import com.lotosia.identityservice.service.email.config.EmailConstants;
import com.lotosia.identityservice.service.email.model.EmailMessage;
import com.lotosia.identityservice.exception.RetryableEmailException;
import com.lotosia.identityservice.service.email.queue.EmailQueueService;
import com.lotosia.identityservice.service.email.retry.EmailRetryHandler;
import com.lotosia.identityservice.service.email.sender.AsyncEmailSender;
import com.lotosia.identityservice.service.email.util.EmailContentBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final EmailRetryHandler retryHandler;
    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;

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

            if (!retryHandler.isReadyForProcessing(message)) {
                acknowledgment.acknowledge();
                retryHandler.scheduleRetry(message);
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
            asyncEmailSender.sendPreRenderedEmail(
                message.getRecipientEmail(),
                message.getSubject(),
                message.getPlainText(),
                message.getHtmlContent()
            );
            return true;
        } catch (RetryableEmailException e) {
            if (message.hasExceededMaxRetries(EmailConstants.MAX_RETRY_ATTEMPTS)) {
                return false;
            }
            retryHandler.scheduleRetry(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
    private void handleProcessingFailure(EmailMessage message, Acknowledgment acknowledgment) {
        EmailMessage retryMessage = message.incrementRetryCount();

        if (retryMessage.hasExceededMaxRetries(EmailConstants.MAX_RETRY_ATTEMPTS)) {
            kafkaTemplate.send(EmailConstants.EMAIL_DLQ_TOPIC, message.getRecipientEmail(), retryMessage);
        } else {
            kafkaTemplate.send(EmailConstants.EMAIL_TOPIC, message.getRecipientEmail(), retryMessage);
        }

        acknowledgment.acknowledge();
    }

    private void sendFallbackEmail(EmailMessage message) {
        if (message.getPlainText() != null && message.getSubject() != null) {
            asyncEmailSender.sendSimpleEmail(message.getRecipientEmail(), message.getSubject(), message.getPlainText());
        } else {
            EmailContentBuilder.EmailContent content = EmailContentBuilder.buildFallbackContent(message);
            asyncEmailSender.sendSimpleEmail(message.getRecipientEmail(), content.getSubject(), content.getPlainText());
        }
    }

}
