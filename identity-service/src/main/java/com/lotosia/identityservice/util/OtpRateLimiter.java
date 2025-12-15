package com.lotosia.identityservice.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class OtpRateLimiter {

    private final int limit;
    private final Duration window;
    private final ConcurrentHashMap<String, Deque<Instant>> requests = new ConcurrentHashMap<>();

    public OtpRateLimiter() {
        this.limit = 3;
        this.window = Duration.ofSeconds(5);
    }

    private static String normalize(String v) {
        return v == null ? "" : v.trim();
    }

    public boolean tryAcquire(String rawKey) {
        String key = normalize(rawKey);
        Instant now = Instant.now();

        Deque<Instant> q = requests.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && Duration.between(q.peekFirst(), now).compareTo(window) > 0) {
                q.removeFirst();
            }
            if (q.size() >= limit) return false;
            q.addLast(now);
            return true;
        }
    }
}
