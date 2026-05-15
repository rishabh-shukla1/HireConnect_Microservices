package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(
        name = "job-service",
        path = "/jobs"
)
public interface JobServiceClient {

    @GetMapping("/{jobId}/views")
    Integer getJobViewCount(@PathVariable("jobId") Long jobId);

    @GetMapping("/recruiter/{recruiterId}/count")
    Long getRecruiterJobCount(@PathVariable("recruiterId") UUID recruiterId);

    @GetMapping("/recruiter/{recruiterId}/active/count")
    Long getActiveJobCount(@PathVariable("recruiterId") UUID recruiterId);

    @GetMapping("/recruiter/{recruiterId}/closed/count")
    Long getClosedJobCount(@PathVariable("recruiterId") UUID recruiterId);

    @GetMapping("/categories/top")
    Map<String, Long> getTopCategories();
    
    @GetMapping("/count/active")
    Integer getAllActiveJobsCount();
    
    @GetMapping("/count/total")
    Integer getTotalJobsCount();
}