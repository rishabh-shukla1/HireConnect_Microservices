package com.hireconnect.profile.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class JobServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${JOB_SERVICE_URL:http://job-service:8083}")
    private String jobServiceUrl;

    public JobServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getJobById(Long jobId) {
        try {
            String url = jobServiceUrl + "/jobs/" + jobId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } 
        catch (Exception e) {
            log.error("Error fetching job {}: {}", jobId, e.getMessage());
            return null;
        }
    }
}