package com.lotosia.identityservice.service;

import com.lotosia.identityservice.dto.AuthResponse;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.exception.EmailAlreadyInUseException;
import com.lotosia.identityservice.exception.InvalidCredentialsException;
import com.lotosia.identityservice.exception.NotFoundException;
import com.lotosia.identityservice.repository.UserRepository;
import com.lotosia.identityservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.createTokenWithRole(user.getEmail(), user.getRoles());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

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

            return jwtUtil.createTokenWithRole(email, user.getRoles());

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

        Role roleUser = new Role();
        roleUser.setName("USER");
        roleUser.setUser(newUser);
        newUser.getRoles().add(roleUser);

        User savedUser = userRepository.save(newUser);

        String accessToken = jwtUtil.createTokenWithRole(savedUser.getEmail(), savedUser.getRoles());
        String refreshToken = jwtUtil.createRefreshToken(savedUser.getEmail());

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
}
