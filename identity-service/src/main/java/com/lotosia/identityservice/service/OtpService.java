package com.lotosia.identityservice.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_VALID_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void generateAndSentOtp(RegisterRequest dto) {
        final String email = normalizeEmail(dto.getEmail());
        final String firstName = dto.getFirstName();
        final String lastName = dto.getLastName();
        final String rawPassword = dto.getPassword();

        if (!canRequestOtp(email)) {
            throw new TooManyRequestsException(getRateLimitMessage(email));
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

        otpRepository.save(otpEntity);
        emailService.sendOtpEmailHtml(email, otp);
    }

    private boolean canRequestOtp(String email) {
        String attemptsKey = email + ":otp_attempts";
        String lastAttemptKey = email + ":last_otp_attempt";

        long currentTime = System.currentTimeMillis();
        long fiveMinutesAgo = currentTime - (5 * 60 * 1000);
        long thirtySecondsAgo = currentTime - (30 * 1000);

        redisTemplate.opsForZSet().removeRangeByScore(attemptsKey, 0, fiveMinutesAgo);

        String lastAttemptStr = redisTemplate.opsForValue().get(lastAttemptKey);
        if (lastAttemptStr != null) {
            long lastAttemptTime = Long.parseLong(lastAttemptStr);
            if (lastAttemptTime > thirtySecondsAgo) {
                return false;
            }
        }

        Long attemptCount = redisTemplate.opsForZSet().size(attemptsKey);
        if (attemptCount != null && attemptCount >= 3) {
            return false;
        }

        return true;
    }

    private String getRateLimitMessage(String email) {
        String attemptsKey = email + ":otp_attempts";
        String lastAttemptKey = email + ":last_otp_attempt";

        long currentTime = System.currentTimeMillis();
        long thirtySecondsAgo = currentTime - (30 * 1000);

        String lastAttemptStr = redisTemplate.opsForValue().get(lastAttemptKey);
        if (lastAttemptStr != null) {
            long lastAttemptTime = Long.parseLong(lastAttemptStr);
            if (lastAttemptTime > thirtySecondsAgo) {
                long waitTimeSeconds = ((lastAttemptTime + (30 * 1000)) - currentTime) / 1000;
                return "Please wait " + waitTimeSeconds + " seconds before requesting another OTP.";
            }
        }

        return "Too many OTP requests. Please try again in 5 minutes.";
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Optional<Otp> getOtpByEmail(String rawEmail) {
        return otpRepository.findByEmail(normalizeEmail(rawEmail));
    }

    @Transactional
    public void clearOtpData(String rawEmail) {
        otpRepository.findByEmail(normalizeEmail(rawEmail)).ifPresent(otpRepository::delete);
    }
}
