package com.lotosia.identityservice.controller;

import com.lotosia.identityservice.dto.AuthResponse;
import com.lotosia.identityservice.dto.LoginRequest;
import com.lotosia.identityservice.dto.LoginResult;
import com.lotosia.identityservice.dto.RefreshResult;
import com.lotosia.identityservice.dto.RefreshTokenRequest;
import com.lotosia.identityservice.dto.RefreshTokenResponse;
import com.lotosia.identityservice.dto.RegisterRequest;
import com.lotosia.identityservice.dto.RegisterResult;
import com.lotosia.identityservice.dto.ResetPasswordRequest;
import com.lotosia.identityservice.entity.Otp;
import com.lotosia.identityservice.exception.InvalidCredentialsException;
import com.lotosia.identityservice.exception.NotFoundException;
import com.lotosia.identityservice.service.AuthService;
import com.lotosia.identityservice.service.OtpService;
import com.lotosia.identityservice.util.AuthenticationUtil;
import com.lotosia.identityservice.util.CookieUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final CookieUtil cookieUtil;
    private final AuthenticationUtil authenticationUtil;

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
    public ResponseEntity<?> verifyOtpAndRegister(@RequestParam String email, @RequestParam String otp, HttpServletResponse response) {
        otpService.verifyOtpOrThrow(email, otp);

        Optional<Otp> optionalOtpEntity = otpService.getOtpByEmail(email);
        if (optionalOtpEntity.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "REG_DATA_MISSING",
                    "message", "No pending registration data found for this email. Please request OTP again."
            ));
        }

        Otp otpEntity = optionalOtpEntity.get();

        RegisterResult result = authService.registerWithTokens(
                otpEntity.getFirstName(),
                otpEntity.getLastName(),
                otpEntity.getEmail(),
                otpEntity.getHashedPassword()
        );

        cookieUtil.addAccessTokenCookie(response, result.getAccessToken());
        cookieUtil.addRefreshTokenCookie(response, result.getRefreshToken());

        otpService.clearOtpData(email);

        return new ResponseEntity<>(result.getAuthResponse(), HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResult result = authService.loginWithTokens(loginRequest.getEmail(), loginRequest.getPassword());

        cookieUtil.addAccessTokenCookie(response, result.getAccessToken());
        cookieUtil.addRefreshTokenCookie(response, result.getRefreshToken());

        return new ResponseEntity<>(result.getAuthResponse(), HttpStatus.OK);
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieUtil.getAccessTokenFromCookies(request);
        cookieUtil.clearAccessTokenCookie(response);
        cookieUtil.clearRefreshTokenCookie(response);

        if (token != null) {
            return authService.logout("Bearer " + token);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token not found in cookies"));
        }

        try {
            RefreshResult result = authService.refreshWithTokens(refreshToken);

            cookieUtil.addAccessTokenCookie(response, result.getRefreshTokenResponse().getAccessToken());
            cookieUtil.addRefreshTokenCookie(response, result.getNewRefreshToken());

            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> getCurrentUser(HttpServletRequest request) {
        try {
            String token = cookieUtil.getAccessTokenFromCookies(request);

            if (token == null) {
                // Check headers from API Gateway
                String userEmail = request.getHeader("X-User-Email");

                if (userEmail != null && !userEmail.isEmpty()) {
                    AuthResponse userInfo = authService.getCurrentUserInfo(userEmail);
                    return ResponseEntity.ok(userInfo);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }

            AuthResponse userInfo = authService.getCurrentUserFromToken(token);
            return ResponseEntity.ok(userInfo);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
