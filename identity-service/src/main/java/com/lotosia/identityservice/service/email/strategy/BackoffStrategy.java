package com.lotosia.identityservice.service.email.strategy;

public interface BackoffStrategy {

    long calculateDelay(int retryCount);
}