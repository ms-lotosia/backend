package com.lotosia.identityservice.service.email.sender;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
public class EmailErrorClassifier {

    public boolean isRetryable(MailException exception) {
        String message = exception.getMessage();
        if (message == null) {
            Throwable cause = exception.getCause();
            if (cause != null) {
                message = cause.getMessage();
            }
        }

        if (message == null) {
            return false;
        }

        String lowerMessage = message.toLowerCase();
        return containsRetryableCode(lowerMessage) || containsRetryableText(lowerMessage);
    }

    private boolean containsRetryableCode(String message) {
        return message.contains("421") ||
               message.contains("450") ||
               message.contains("451") ||
               message.contains("452") ||
               message.contains("454") ||
               message.contains("455");
    }

    private boolean containsRetryableText(String message) {
        return message.contains("temporary") ||
               message.contains("rate limit") ||
               message.contains("throttle") ||
               message.contains("try again");
    }
}