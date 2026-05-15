package com.hireconnect.auth.config;

import com.hireconnect.auth.entity.Provider;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.entity.UserRole;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.service.RefreshTokenService;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String extractedEmail = oauthUser.getAttribute("email");

        if (extractedEmail == null) {
            Map<String, Object> attributes = oauthUser.getAttributes();

            if (!Collections.isEmpty(attributes) && attributes.containsKey("login")) {
                extractedEmail = attributes.get("login") + "@github.oauth";
            }
        }

        final String email = extractedEmail;

        UserCredential user = authRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserCredential newUser = UserCredential.builder()
                            .email(email)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(UserRole.CANDIDATE)
                            .provider(Provider.GITHUB)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return authRepository.save(newUser);
                });

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        String frontendUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:5173");
        String redirectUrl = frontendUrl + "/auth/callback";

        response.sendRedirect(
                redirectUrl
                        + "?token=" + accessToken
                        + "&refreshToken=" + refreshToken
                        + "&role=" + user.getRole().name()
                        + "&userId=" + user.getId()
                        + "&email=" + user.getEmail()
        );
    }
}