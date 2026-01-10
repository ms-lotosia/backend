package com.lotosia.identityservice.service.email.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern OTP_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://(?!.*(?:\\.{2,}|localhost|127\\.0\\.0\\.1|0\\.0\\.0\\.0)).+"
    );

    public void validateEmailAddress(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }

        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
    }

    public void validateOtpCode(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP code cannot be null or empty");
        }

        if (!OTP_PATTERN.matcher(otp.trim()).matches()) {
            throw new IllegalArgumentException("OTP code must be exactly 6 digits");
        }
    }

    public void validateResetLink(String resetLink) {
        if (resetLink == null || resetLink.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset link cannot be null or empty");
        }

        if (!URL_PATTERN.matcher(resetLink.trim()).matches()) {
            throw new IllegalArgumentException("Reset link must use HTTP or HTTPS protocol and cannot contain localhost/internal addresses");
        }
    }

    public boolean isValidEmailFormat(String email) {
        return email != null && !email.trim().isEmpty() &&
               EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public boolean isValidOtpFormat(String otp) {
        return otp != null && !otp.trim().isEmpty() &&
               OTP_PATTERN.matcher(otp.trim()).matches();
    }

    public boolean isValidUrlFormat(String url) {
        return url != null && !url.trim().isEmpty() &&
               URL_PATTERN.matcher(url.trim()).matches();
    }
}
