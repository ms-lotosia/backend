package com.lotosia.identityservice.service;

import com.lotosia.identityservice.dto.RegisterRequest;
import com.lotosia.identityservice.entity.Otp;
import com.lotosia.identityservice.exception.ExpiredOtpException;
import com.lotosia.identityservice.exception.InvalidOtpException;
import com.lotosia.identityservice.exception.TooManyRequestsException;
import com.lotosia.identityservice.repository.OtpRepository;
import com.lotosia.identityservice.util.OtpRateLimiter;
import lombok.RequiredArgsConstructor;
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
    private final OtpRateLimiter limiter;

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void generateAndSentOtp(RegisterRequest dto) {
        final String email = normalizeEmail(dto.getEmail());
        final String firstName = dto.getFirstName();
        final String lastName = dto.getLastName();
        final String rawPassword = dto.getPassword();

        if (!limiter.tryAcquire(email)) {
            throw new TooManyRequestsException("You can only request 3 OTPs every 5 minutes");
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
        emailService.sendOtpEmail(email, otp);
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
