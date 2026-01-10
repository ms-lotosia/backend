package com.lotosia.identityservice.service.email.strategy;

import com.lotosia.identityservice.config.EmailConstants;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ExponentialBackoffWithJitter implements BackoffStrategy {

    private final Random random = new Random();

    @Override
    public long calculateDelay(int retryCount) {
        long baseDelay = (long) (EmailConstants.BASE_RETRY_DELAY_MS * Math.pow(2, retryCount));
        long maxDelay = Math.min(baseDelay, EmailConstants.MAX_RETRY_DELAY_MS);
        long jitterRange = (long) (maxDelay * EmailConstants.JITTER_FACTOR);
        long jitter = random.nextLong() % (jitterRange * 2) - jitterRange;
        return Math.max(0, maxDelay + jitter);
    }
}