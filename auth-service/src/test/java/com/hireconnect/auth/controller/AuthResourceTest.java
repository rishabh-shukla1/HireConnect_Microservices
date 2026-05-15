package com.hireconnect.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.auth.config.JwtAuthenticationFilter;
import com.hireconnect.auth.config.OAuth2LoginSuccessHandler;
import com.hireconnect.auth.dto.AuthResponse;
import com.hireconnect.auth.dto.LoginRequest;
import com.hireconnect.auth.dto.RefreshTokenRequest;
import com.hireconnect.auth.dto.SignupRequest;
import com.hireconnect.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnAuthResponse() throws Exception {

        SignupRequest request = new SignupRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setMobile("9876543210");
        request.setFullName("Test User");
        request.setRole("CANDIDATE");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .email("test@gmail.com")
                .role("CANDIDATE")
                .message("Registration successful")
                .build();

        when(authService.register(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"));
    }

    @Test
    void login_ShouldReturnAuthResponse() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .email("test@gmail.com")
                .role("CANDIDATE")
                .message("Login successful")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void logout_ShouldReturnSuccessMessage() throws Exception {

        doNothing().when(authService).logout("jwt-token");

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }

    @Test
    void refreshToken_ShouldReturnNewToken() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        AuthResponse response = AuthResponse.builder()
                .token("new-jwt-token")
                .refreshToken("refresh-token")
                .message("Token refreshed successfully")
                .build();

        when(authService.refreshToken("refresh-token")).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"));
    }

    @Test
    void validateToken_ShouldReturnUserDetails_WhenTokenIsValid() throws Exception {

        when(authService.validateToken("jwt-token")).thenReturn(true);
        when(authService.extractEmail("jwt-token")).thenReturn("test@gmail.com");
        when(authService.extractRole("jwt-token")).thenReturn("CANDIDATE");
        when(authService.extractUserId("jwt-token")).thenReturn("123");

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.userId").value("123"));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() throws Exception {

        when(authService.validateToken("jwt-token")).thenReturn(false);

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}