package com.hireconnect.application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ProfileServiceClient {

    private final RestTemplate restTemplate;

    @Value("${PROFILE_SERVICE_URL:http://profile-service:8082}")
    private String profileServiceUrl;

    public ProfileServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches a candidate profile forwarding the caller's Bearer token so the
     * profile service's @PreAuthorize passes. If token is null we fall back to
     * an anonymous GET which will return 403 — caller should handle the empty
     * map gracefully.
     */
    @SuppressWarnings("rawtypes")
    public Map<String, Object> getCandidate(UUID userId, String bearerToken) {
        try {
            String url = profileServiceUrl + "/profiles/candidates/" + userId;
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, bearerToken.startsWith("Bearer ")
                        ? bearerToken
                        : "Bearer " + bearerToken);
            }
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return body != null ? body : Collections.emptyMap();
        } catch (Exception e) {
            log.warn("Could not fetch candidate {}: {}", userId, e.getMessage());
            return Collections.emptyMap();
        }
    }
}
