package com.lotosia.identityservice.service.email.retry;

import com.lotosia.identityservice.service.email.model.EmailMessage;

public interface EmailRetryHandler {

    void scheduleRetry(EmailMessage message);

    boolean isReadyForProcessing(EmailMessage message);
}