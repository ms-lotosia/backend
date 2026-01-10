package com.lotosia.identityservice.service.email.retry;

import com.lotosia.identityservice.dto.EmailMessage;

public interface EmailRetryHandler {

    void scheduleRetry(EmailMessage message);

    boolean isReadyForProcessing(EmailMessage message);
}