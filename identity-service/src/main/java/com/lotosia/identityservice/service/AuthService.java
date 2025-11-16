package com.lotosia.identityservice.service;

import com.lotosia.identityservice.dto.AuthResponse;
import com.lotosia.identityservice.entity.Role;
import com.lotosia.identityservice.entity.User;
import com.lotosia.identityservice.exception.EmailAlreadyInUseException;
import com.lotosia.identityservice.exception.InvalidCredentialsException;
import com.lotosia.identityservice.repository.UserRepository;
import com.lotosia.identityservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Set<String> blackListedTokes = new HashSet<>();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }


        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtUtil.createTokenWithRole(user.getEmail(), roles);
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        return buildAuthResponseDto(user, accessToken, refreshToken);
    }

    public ResponseEntity<?> logout(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        if (blackListedTokes.contains(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You are already logged out.");
        }

        blackListedTokes.add(token);
        String email = jwtUtil.getEmailFromToken(token);
        jwtUtil.invalidateToken(email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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

        Set<String> roleNames = savedUser.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtUtil.createTokenWithRole(
                savedUser.getEmail(),
                roleNames
        );

        String refreshToken = jwtUtil.createRefreshToken(savedUser.getEmail());

        return buildAuthResponseDto(savedUser, accessToken, refreshToken);
    }

    private AuthResponse buildAuthResponseDto(User user, String accessToken, String refreshToken) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);

        if(user != null){
            authResponse.setEmail(user.getEmail());
            authResponse.setFirstName(user.getFirstName());
            authResponse.setLastName(user.getLastName());



            Set<String> roleNames = user.getRoles()
                    .stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            authResponse.setRoles(roleNames);
        }

        return authResponse;
    }
}
