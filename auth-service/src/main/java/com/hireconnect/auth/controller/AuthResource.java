package com.hireconnect.auth.controller;

import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.LoginRequest;
import com.hireconnect.auth.dto.RefreshTokenRequest;
import com.hireconnect.auth.dto.SignupRequest;
import com.hireconnect.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        service.logout(token);

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(service.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(false);
        }
        
        String token = authorizationHeader.substring(7);

        boolean isValid = service.validateToken(token);
        if (!isValid) {
            return ResponseEntity.status(401).body(false);
        }

        AuthResponse userDetails = AuthResponse.builder()
                .email(service.extractEmail(token))
                .role(service.extractRole(token))
                .userId(service.extractUserId(token))
                .build();

        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/extract/email")
    public ResponseEntity<String> extractEmail(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");

        return ResponseEntity.ok(service.extractEmail(token));
    }

    @GetMapping("/extract/role")
    public ResponseEntity<String> extractRole(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");

        return ResponseEntity.ok(service.extractRole(token));
    }

    @GetMapping("/extract/userId")
    public ResponseEntity<String> extractUserId(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");

        return ResponseEntity.ok(service.extractUserId(token));
    }
}