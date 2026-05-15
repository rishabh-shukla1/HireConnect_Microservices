package com.hireconnect.auth.service;

import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.LoginRequest;
import com.hireconnect.auth.dto.SignupRequest;

public interface AuthService {

    AuthResponse register(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    boolean validateToken(String token);

    AuthResponse refreshToken(String refreshToken);

    String extractEmail(String token);

    String extractRole(String token);

    String extractUserId(String token);
}