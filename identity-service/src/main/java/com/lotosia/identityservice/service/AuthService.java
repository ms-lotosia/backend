package com.lotosia.identityservice.service;

import com.lotosia.identityservice.client.BasketClient;
import com.lotosia.identityservice.client.ProfileClient;
import com.lotosia.identityservice.dto.AuthResponse;
import com.lotosia.identityservice.dto.CreateBasketRequest;
import com.lotosia.identityservice.dto.ProfileRequest;
import com.lotosia.identityservice.dto.RegisterRequest;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.exception.EmailAlreadyInUseException;
import com.lotosia.identityservice.exception.InvalidCredentialsException;
import com.lotosia.identityservice.exception.NotFoundException;
import com.lotosia.identityservice.repository.RoleRepository;
import com.lotosia.identityservice.repository.UserRepository;
import com.lotosia.identityservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final ProfileClient profileClient;
    private final BasketClient basketClient;

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.createTokenWithRole(user.getEmail(), user.getId(), user.getRoles());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), user.getId());

        return buildAuthResponseDto(user, accessToken, refreshToken);
    }

    public ResponseEntity<?> logout(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        String redisKey = "blacklist:" + token;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You are already logged out.");
        }

        long expiration = jwtUtil.getExpirationTime(token);
        redisTemplate.opsForValue().set(redisKey, "1", expiration, TimeUnit.MILLISECONDS);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public void sendResetPasswordLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with this email does not exist"));

        String redisKey = email + ":resit";
        String existingToken = redisTemplate.opsForValue().get(redisKey);

        if (existingToken != null) {
            redisTemplate.delete(redisKey);
        }

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(redisKey, resetToken, 1, TimeUnit.HOURS);

        String resetLink = "https://lotosia.vercel.app/reset-password?token=" + resetToken;
        emailService.sendResetPasswordEmailHtml(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        String email = redisTemplate.keys("*:reset").stream()
                .filter(key -> {
                    String value = redisTemplate.opsForValue().get(key);
                    return value != null && value.equals(token);
                })
                .map(key -> key.replace(":reset", ""))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with this email does not exist"));
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redisTemplate.delete(email + ":reset");
    }

    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        try {
            String email = jwtUtil.getEmailFromToken(refreshToken);
            String redisKey = "refresh:" + refreshToken;
            String storedEmail = redisTemplate.opsForValue().get(redisKey);

            if (storedEmail == null || !storedEmail.equals(email)) {
                throw new SecurityException("Invalid or expired refresh token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException(
                            "USER_NOT_FOUND",
                            String.format("User with this email does not exist: %s", email)
                    ));

            return jwtUtil.createTokenWithRole(email, user.getId(), user.getRoles());

        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("Invalid refresh token: " + e.getMessage());
        }
    }

    public boolean isUserExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public AuthResponse registerUserWithHashedPassword(String firstName, String lastName, String email, String hashedPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Email is already in use");
        }

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);

        Role roleUser = roleRepository.findByName("USER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("USER");
            return newRole;
        });
        roleUser.setUser(newUser);
        newUser.getRoles().add(roleUser);

        User savedUser = userRepository.save(newUser);

        try {
            ProfileRequest profileRequest = ProfileRequest.builder()
                    .userId(savedUser.getId())
                    .build();

            profileClient.createProfile(profileRequest);
        } catch (Exception e) {
        }

        try {
            CreateBasketRequest basketRequest = CreateBasketRequest.builder()
                    .userId(savedUser.getId())
                    .build();

            basketClient.createBasket(basketRequest);
        } catch (Exception e) {
        }

        String accessToken = jwtUtil.createTokenWithRole(savedUser.getEmail(), savedUser.getId(), savedUser.getRoles());
        String refreshToken = jwtUtil.createRefreshToken(savedUser.getEmail(), savedUser.getId());

        return buildAuthResponseDto(savedUser, accessToken, refreshToken);
    }

    private AuthResponse buildAuthResponseDto(User user, String accessToken, String refreshToken) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);

        if (user != null) {
            authResponse.setEmail(user.getEmail());
            authResponse.setFirstName(user.getFirstName());
            authResponse.setLastName(user.getLastName());

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            authResponse.setRoles(roleNames);
        }

        return authResponse;
    }

    private void validatePassword(String password) {
        RegisterRequest tempDto = new RegisterRequest();
        tempDto.setPassword(password);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(tempDto);
        for (ConstraintViolation<RegisterRequest> violation : violations) {
            if ("password".equals(violation.getPropertyPath().toString())) {
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}
