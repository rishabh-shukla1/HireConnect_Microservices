package com.hireconnect.job.client;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "analytics-service")
public interface AnalyticsClient {

    @GetMapping("/analytics/job/{jobId}/views")
    Integer getJobViewCount(@PathVariable("jobId") Long jobId);
}