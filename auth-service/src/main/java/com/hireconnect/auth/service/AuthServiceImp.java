package com.hireconnect.auth.service;

import java.util.UUID;

import com.hireconnect.auth.client.NotificationClient;
import com.hireconnect.auth.config.JwtService;
import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.EmailRequest;
import com.hireconnect.auth.dto.LoginRequest;
import com.hireconnect.auth.dto.SignupRequest;
import com.hireconnect.auth.entity.Provider;
import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.entity.UserRole;
import com.hireconnect.auth.event.UserCreatedEvent;
import com.hireconnect.auth.exception.EmailAlreadyExistsException;
import com.hireconnect.auth.exception.InvalidCredentialsException;
import com.hireconnect.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private static final String BLACKLIST_PREFIX = "blacklisted_token:";

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final UserEventPublisher userEventPublisher;
    private final RefreshTokenService refreshTokenService;
    private final NotificationClient notificationClient;

    @Override
    public AuthResponse register(SignupRequest request) {

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        UserCredential user = UserCredential.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .mobileNumber(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.valueOf(request.getRole().toUpperCase()))
                .provider(Provider.LOCAL)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .active(true)
                .build();

        user = authRepository.save(user);

        notificationClient.sendEmail(new EmailRequest(
            request.getEmail(),
            "Welcome to HireConnect 🎉",
            "Hi " + request.getFullName() + ",\n\n" +
            "Your HireConnect account has been created successfully.\n" +
            "You can now log in and start using the platform."
        ));

        userEventPublisher.publishUserCreatedEvent(
                UserCreatedEvent.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .mobileNumber(user.getMobileNumber())
                        .build()
        );

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserCredential user = authRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        authRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    @Override
    public void logout(String token) {

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "LOGGED_OUT",
                Duration.ofHours(24)
        );
    }

    @Override
    public boolean validateToken(String token) {

        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
            return false;
        }

        return jwtService.validateToken(token);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenService.validateRefreshToken(refreshToken);

        UserCredential user = authRepository.findById(storedToken.getUserId()).orElseThrow(() ->
                        new InvalidCredentialsException("User not found"));

        String newAccessToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(storedToken.getToken())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Token refreshed successfully")
                .build();
    }

    @Override
    public String extractEmail(String token) {
        return jwtService.extractEmail(token);
    }

    @Override
    public String extractRole(String token) {
        return jwtService.extractRole(token);
    }

    @Override
    public String extractUserId(String token) {
        return jwtService.extractUserId(token);
    }
}