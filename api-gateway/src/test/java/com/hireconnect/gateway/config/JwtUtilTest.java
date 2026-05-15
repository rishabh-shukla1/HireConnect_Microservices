package com.hireconnect.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 * Validates token creation, validation, email extraction, and
 * handling of expired/malformed tokens without needing Spring context.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    /** Must match jwt.secret in application.properties for signing consistency. */
    private static final String TEST_SECRET = "hireconnect-jwt-secret-key-2026-secure-global";

    /**
     * Creates a fresh JwtUtil instance before each test and injects
     * the test secret via reflection (simulating @Value injection).
     */
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    }

    /**
     * Helper: builds a signed JWT with the given subject, expiration,
     * and optional custom claims.
     */
    private String buildToken(String subject, Date expiration, String role, String userId) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiration);

        if (role != null) builder.claim("role", role);
        if (userId != null) builder.claim("userId", userId);

        return builder.signWith(key).compact();
    }

    // ─── validateToken ──────────────────────────────────────────

    /**
     * A token with a future expiration should be valid.
     */
    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        String token = buildToken("user@test.com",
                new Date(System.currentTimeMillis() + 3600_000), "CANDIDATE", "uuid-1");

        assertTrue(jwtUtil.validateToken(token));
    }

    /**
     * A token that has already expired should be invalid.
     */
    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        String token = buildToken("user@test.com",
                new Date(System.currentTimeMillis() - 1000), "CANDIDATE", "uuid-1");

        assertFalse(jwtUtil.validateToken(token));
    }

    /**
     * A completely garbage string should be invalid, not throw.
     */
    @Test
    void validateToken_MalformedToken_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("not.a.jwt"));
    }

    // ─── extractEmail ───────────────────────────────────────────

    /**
     * Verifies the subject (email) is correctly extracted.
     */
    @Test
    void extractEmail_ShouldReturnSubject() {
        String token = buildToken("recruiter@hireconnect.com",
                new Date(System.currentTimeMillis() + 3600_000), "RECRUITER", "uuid-2");

        assertEquals("recruiter@hireconnect.com", jwtUtil.extractEmail(token));
    }

    // ─── extractClaims ──────────────────────────────────────────

    /**
     * Verifies that custom claims (role, userId) are accessible.
     */
    @Test
    void extractClaims_ShouldContainCustomClaims() {
        String token = buildToken("admin@hireconnect.com",
                new Date(System.currentTimeMillis() + 3600_000), "ADMIN", "admin-uuid");

        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("admin@hireconnect.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals("admin-uuid", claims.get("userId", String.class));
    }
}
