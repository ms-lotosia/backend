package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.dto.AuthResponse;
import com.lotosia.identityservice.dto.LoginRequest;
import com.lotosia.identityservice.dto.RefreshTokenRequest;
import com.lotosia.identityservice.dto.RegisterRequest;
import com.lotosia.identityservice.dto.ResetPasswordRequest;
import com.lotosia.identityservice.entity.Otp;
import com.lotosia.identityservice.service.AuthService;
import com.lotosia.identityservice.service.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody RegisterRequest dto) {
        if (authService.isUserExists(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("code", "EMAIL_ALREADY_REGISTERED", "message", "Email already registered."));
        }

        otpService.generateAndSentOtp(dto);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to " + dto.getEmail() + ". Please verify to complete registration."
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpAndRegister(@RequestParam String email, @RequestParam String otp) {
        otpService.verifyOtpOrThrow(email, otp);

        Optional<Otp> optionalOtpEntity = otpService.getOtpByEmail(email);
        if (optionalOtpEntity.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "REG_DATA_MISSING",
                    "message", "No pending registration data found for this email. Please request OTP again."
            ));
        }

        Otp otpEntity = optionalOtpEntity.get();

        AuthResponse response = authService.registerUserWithHashedPassword(
                otpEntity.getFirstName(),
                otpEntity.getLastName(),
                otpEntity.getEmail(),
                otpEntity.getHashedPassword()
        );

        otpService.clearOtpData(email);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token);
    }

    @PostMapping("/send-reset-password-link")
    public ResponseEntity<Void> sendResetPasswordLink(@RequestParam String email) {
        authService.sendResetPasswordLink(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
