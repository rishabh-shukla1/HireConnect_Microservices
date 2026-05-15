package com.hireconnect.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            System.out.println("JWT VALID");
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("Expiration: " + claims.getExpiration());

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            System.out.println("JWT INVALID");
            e.printStackTrace();
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims extractClaims(String token) {
        return extractAllClaims(token);
    }
}