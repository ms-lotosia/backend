package com.lotosia.identityservice.service.email.retry;

import com.lotosia.identityservice.service.email.config.EmailConstants;
import com.lotosia.identityservice.service.email.model.EmailMessage;
import com.lotosia.identityservice.service.email.strategy.BackoffStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailRetryService implements EmailRetryHandler {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private final BackoffStrategy backoffStrategy;

    @Override
    public void scheduleRetry(EmailMessage message) {
        int retryCount = message.getRetryCount();
        long delayMillis = backoffStrategy.calculateDelay(retryCount);

        EmailMessage retryMessage = EmailMessage.builder()
                .type(message.getType())
                .recipientEmail(message.getRecipientEmail())
                .otpCode(message.getOtpCode())
                .resetLink(message.getResetLink())
                .subject(message.getSubject())
                .plainText(message.getPlainText())
                .htmlContent(message.getHtmlContent())
                .timestamp(System.currentTimeMillis() + delayMillis)
                .retryCount(retryCount + 1)
                .build();

        kafkaTemplate.send(EmailConstants.EMAIL_TOPIC, message.getRecipientEmail(), retryMessage);
    }

    @Override
    public boolean isReadyForProcessing(EmailMessage message) {
        return message.getTimestamp() <= System.currentTimeMillis();
    }
}