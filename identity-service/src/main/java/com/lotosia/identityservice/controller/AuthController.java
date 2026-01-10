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
import com.lotosia.identityservice.service.security.AuthenticationUtil;
import com.lotosia.identityservice.web.ControllerUtils;
import com.lotosia.identityservice.security.CookieUtil;
import com.lotosia.identityservice.security.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtUtil jwtUtil;

    @PostMapping("/request-otp")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody RegisterRequest dto) {
        if (authService.isUserExists(dto.getEmail())) {
            return ControllerUtils.conflictResponse("EMAIL_ALREADY_REGISTERED", "Email already registered.");
        }

        otpService.generateAndSentOtp(dto);

        return ControllerUtils.successResponse("OTP sent to " + dto.getEmail() + ". Please verify to complete registration.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtpAndRegister(@RequestParam String email, @RequestParam String otp, HttpServletResponse response) {
        otpService.verifyOtpOrThrow(email, otp);

        Optional<Otp> optionalOtpEntity = otpService.getOtpByEmail(email);
        if (optionalOtpEntity.isEmpty()) {
            return ControllerUtils.badRequestResponse("REG_DATA_MISSING",
                    "No pending registration data found for this email. Please request OTP again.");
        }

        Otp otpEntity = optionalOtpEntity.get();

        RegisterResult result = authService.registerWithTokens(
                otpEntity.getFirstName(),
                otpEntity.getLastName(),
                otpEntity.getEmail(),
                otpEntity.getHashedPassword()
        );

        ControllerUtils.addAuthCookies(cookieUtil, response, result.getAccessToken(), result.getRefreshToken());

        otpService.clearOtpData(email);

        return new ResponseEntity<>(result.getAuthResponse(), HttpStatus.CREATED);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResult result = authService.loginWithTokens(loginRequest.getEmail(), loginRequest.getPassword());

        ControllerUtils.addAuthCookies(cookieUtil, response, result.getAccessToken(), result.getRefreshToken(), result.getCsrfToken());

        return new ResponseEntity<>(result.getAuthResponse(), HttpStatus.OK);
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = ControllerUtils.extractToken(cookieUtil, request);
        ControllerUtils.clearAuthCookies(cookieUtil, response);

        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-reset-password-link")
    public ResponseEntity<Map<String, String>> sendResetPasswordLink(@RequestParam String email) {
        try {
            authService.sendResetPasswordLink(email);
        } catch (Exception e) {
        }
        return ControllerUtils.successResponse("If an account with this email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = ControllerUtils.extractRefreshToken(cookieUtil, request);

        if (refreshToken == null) {
            return ControllerUtils.unauthorizedResponse("Refresh token not found in cookies");
        }

        try {
            RefreshResult result = authService.refreshWithTokens(refreshToken);

            ControllerUtils.addAuthCookies(cookieUtil, response,
                    result.getRefreshTokenResponse().getAccessToken(),
                    result.getNewRefreshToken());

            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ControllerUtils.unauthorizedResponse(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ControllerUtils.badRequestResponse("INVALID_TOKEN", e.getMessage());
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> getCurrentUser(HttpServletRequest request) {
        try {
            String token = ControllerUtils.extractToken(cookieUtil, request);
            String userEmail = request.getHeader("X-User-Email");

            AuthResponse userInfo = authService.getCurrentUser(token, userEmail);
            return ResponseEntity.ok(userInfo);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
