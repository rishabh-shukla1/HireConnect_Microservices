package com.hireconnect.application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JobServiceClient {

    private final RestTemplate restTemplate;

    @Value("${JOB_SERVICE_URL:http://job-service:8083}")
    private String jobServiceUrl;

    public JobServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Object> getJob(Long jobId) {
        try {
            String url = jobServiceUrl + "/jobs/" + jobId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return body != null ? body : Collections.emptyMap();
        } catch (Exception e) {
            log.warn("Could not fetch job {}: {}", jobId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public UUID getPostedBy(Long jobId) {
        Object postedBy = getJob(jobId).get("postedBy");
        if (postedBy == null) return null;
        try {
            return UUID.fromString(postedBy.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
