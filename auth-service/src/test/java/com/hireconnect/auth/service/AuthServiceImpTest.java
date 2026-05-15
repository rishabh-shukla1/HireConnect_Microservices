package com.hireconnect.auth.service;

import com.hireconnect.auth.config.JwtService;
import com.hireconnect.auth.dto.AuthResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImp authService;

    @Test
    void register_ShouldReturnAuthResponse() {

        SignupRequest request = new SignupRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setMobile("9876543210");
        request.setFullName("Test User");
        request.setRole("CANDIDATE");

        UserCredential savedUser = UserCredential.builder()
                .id(UUID.randomUUID())
                .email("test@gmail.com")
                .mobileNumber("9876543210")
                .password("encodedPassword")
                .role(UserRole.CANDIDATE)
                .provider(Provider.LOCAL)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh-token")
                .userId(savedUser.getId())
                .build();

        when(authRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(authRepository.save(any(UserCredential.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("test@gmail.com", response.getEmail());
        assertEquals("CANDIDATE", response.getRole());

        verify(userEventPublisher, times(1))
                .publishUserCreatedEvent(any(UserCreatedEvent.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {

        SignupRequest request = new SignupRequest();
        request.setEmail("test@gmail.com");

        when(authRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(request));
    }

    @Test
    void login_ShouldReturnAuthResponse() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        UserCredential user = UserCredential.builder()
                .id(UUID.randomUUID())
                .email("test@gmail.com")
                .password("encodedPassword")
                .role(UserRole.CANDIDATE)
                .active(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .userId(user.getId())
                .build();

        when(authRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("wrong-password");

        UserCredential user = UserCredential.builder()
                .email("test@gmail.com")
                .password("encodedPassword")
                .active(true)
                .build();

        when(authRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong-password", "encodedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void logout_ShouldBlacklistToken() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout("abc-token");

        verify(valueOperations).set(
                eq("blacklisted_token:abc-token"),
                eq("LOGGED_OUT"),
                any()
        );
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenBlacklisted() {

        when(redisTemplate.hasKey("blacklisted_token:test-token"))
                .thenReturn(true);

        boolean result = authService.validateToken("test-token");

        assertFalse(result);
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken() {

        UUID userId = UUID.randomUUID();

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .userId(userId)
                .build();

        UserCredential user = UserCredential.builder()
                .id(userId)
                .email("test@gmail.com")
                .role(UserRole.CANDIDATE)
                .build();

        when(refreshTokenService.validateRefreshToken("refresh-token"))
                .thenReturn(refreshToken);

        when(authRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("new-jwt-token");

        AuthResponse response = authService.refreshToken("refresh-token");

        assertEquals("new-jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }
}