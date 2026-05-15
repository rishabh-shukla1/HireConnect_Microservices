package com.hireconnect.profile.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/uploads/**",
                                "/profiles/uploads/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/profiles/candidates/**")
                        .hasAnyRole("CANDIDATE", "RECRUITER")

                        .requestMatchers(HttpMethod.POST, "/profiles/candidates")
                        .hasRole("CANDIDATE")

                        .requestMatchers(HttpMethod.PUT, "/profiles/candidates/**")
                        .hasRole("CANDIDATE")

                        .requestMatchers(HttpMethod.GET, "/profiles/recruiters/**")
                        .hasAnyRole("RECRUITER", "CANDIDATE")

                        .requestMatchers(HttpMethod.POST, "/profiles/recruiters")
                        .hasRole("RECRUITER")

                        .requestMatchers(HttpMethod.PUT, "/profiles/recruiters/**")
                        .hasRole("RECRUITER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}