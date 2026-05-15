package com.hireconnect.auth.service;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.exception.InvalidCredentialsException;
import com.hireconnect.auth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(UserCredential user) {

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(UUID.randomUUID().toString())
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() ->
                        new InvalidCredentialsException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token expired");
        }

        return refreshToken;
    }
}