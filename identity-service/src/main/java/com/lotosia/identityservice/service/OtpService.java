package com.lotosia.identityservice.service;

import com.lotosia.identityservice.config.EmailProperties;
import com.lotosia.identityservice.dto.RegisterRequest;
import com.lotosia.identityservice.entity.Otp;
import com.lotosia.identityservice.exception.ExpiredOtpException;
import com.lotosia.identityservice.exception.InvalidOtpException;
import com.lotosia.identityservice.exception.TooManyRequestsException;
import com.lotosia.identityservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_VALID_MINUTES = EmailProperties.OTP_EXPIRY_MINUTES;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static class RateLimitResult {
        private final boolean allowed;
        private final long waitTimeSeconds;

        public RateLimitResult(boolean allowed, long waitTimeSeconds) {
            this.allowed = allowed;
            this.waitTimeSeconds = waitTimeSeconds;
        }

        public boolean isAllowed() { return allowed; }
        public long getWaitTimeSeconds() { return waitTimeSeconds; }
    }

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void generateAndSentOtp(RegisterRequest dto) {
        final String email = normalizeEmail(dto.getEmail());
        final String firstName = dto.getFirstName();
        final String lastName = dto.getLastName();
        final String rawPassword = dto.getPassword();

        RateLimitResult rateLimitResult = checkRateLimit(email);
        if (!rateLimitResult.isAllowed()) {
            throw new TooManyRequestsException(getRateLimitMessage(rateLimitResult.getWaitTimeSeconds()));
        }

        final String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        final LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES);

        Otp otpEntity = otpRepository.findByEmail(email).orElseGet(Otp::new);
        otpEntity.setEmail(email);
        otpEntity.setOtpCode(otp);
        otpEntity.setExpirationTime(expirationTime);
        otpEntity.setFirstName(firstName);
        otpEntity.setLastName(lastName);
        otpEntity.setHashedPassword(passwordEncoder.encode(rawPassword));
        otpEntity.setCreatedAt(LocalDateTime.now());

        otpRepository.save(otpEntity);
        try {
            emailService.sendOtpEmailHtml(email, otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email to " + email + ": " + e.getMessage());
        }
    }

    private static final String OTP_RATE_LIMIT_SCRIPT =
        "local attempts_key = KEYS[1] " +
        "local last_attempt_key = KEYS[2] " +
        "local current_time = tonumber(ARGV[1]) " +
        "local five_min_ago = current_time - (5 * 60 * 1000) " +
        "local thirty_sec_ago = current_time - (30 * 1000) " +
        "local max_attempts = 3 " +

        "redis.call('ZREMRANGEBYSCORE', attempts_key, 0, five_min_ago) " +
        "redis.call('EXPIRE', attempts_key, 300) " +

        "local last_attempt = redis.call('GET', last_attempt_key) " +
        "if last_attempt and tonumber(last_attempt) > thirty_sec_ago then " +
        "    local wait_time = math.ceil((tonumber(last_attempt) + 30000 - current_time) / 1000) " +
        "    return {0, wait_time} " +
        "end " +

        "local attempt_count = redis.call('ZCARD', attempts_key) " +
        "if attempt_count >= max_attempts then " +
        "    local oldest = redis.call('ZRANGE', attempts_key, 0, 0, 'WITHSCORES') " +
        "    if oldest and #oldest >= 2 then " +
        "        local oldest_ts = tonumber(oldest[2]) " +
        "        local wait_time = math.ceil((oldest_ts + 300000 - current_time) / 1000) " +
        "        if wait_time < 1 then wait_time = 1 end " +
        "        return {0, wait_time} " +
        "    else " +
        "        return {0, 300} " +
        "    end " +
        "end " +

        "redis.call('ZADD', attempts_key, current_time, tostring(current_time)) " +
        "redis.call('SETEX', last_attempt_key, 300, tostring(current_time)) " +

        "return {1, 0}";

    private RateLimitResult checkRateLimit(String email) {
        String attemptsKey = "otp:rl:" + email + ":attempts";
        String lastAttemptKey = "otp:rl:" + email + ":last";

        long currentTime = System.currentTimeMillis();

        List<String> keys = Arrays.asList(attemptsKey, lastAttemptKey);
        List<String> args = Arrays.asList(String.valueOf(currentTime));

        byte[][] keysAndArgs = new byte[keys.size() + args.size()][];
        int index = 0;

        for (String key : keys) {
            keysAndArgs[index++] = key.getBytes();
        }

        for (String arg : args) {
            keysAndArgs[index++] = arg.getBytes();
        }

        Object result = redisTemplate.execute(
            connection -> connection.eval(OTP_RATE_LIMIT_SCRIPT.getBytes(),
                org.springframework.data.redis.connection.ReturnType.MULTI,
                keys.size(),
                keysAndArgs
            ),
            true
        );

        if (result instanceof List<?> list && list.size() >= 2) {
            long allowed = convertToLong(list.get(0));
            long waitTime = convertToLong(list.get(1));
            return new RateLimitResult(allowed == 1L, waitTime);
        }

        return new RateLimitResult(false, 300L);
    }

    private long convertToLong(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        if (value instanceof byte[]) {
            try {
                return Long.parseLong(new String((byte[]) value));
            } catch (NumberFormatException e) {
                return 0L;
    }
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String getRateLimitMessage(long waitTimeSeconds) {
        if (waitTimeSeconds <= 60) {
                return "Please wait " + waitTimeSeconds + " seconds before requesting another OTP.";
        } else {
        return "Too many OTP requests. Please try again in 5 minutes.";
        }
    }

    public void verifyOtpOrThrow(String rawEmail, String otp) {
        final String email = normalizeEmail(rawEmail);

        Otp entity = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("No OTP found for this email."));

        if (entity.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ExpiredOtpException("OTP has expired. Please request a new one.");
        }

        if (!entity.getOtpCode().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP entered.");
        }
    }

    public Optional<Otp> getOtpByEmail(String rawEmail) {
        return otpRepository.findByEmail(normalizeEmail(rawEmail));
    }

    public void clearOtpData(String rawEmail) {
        otpRepository.findByEmail(normalizeEmail(rawEmail)).ifPresent(otpRepository::delete);
    }

}
