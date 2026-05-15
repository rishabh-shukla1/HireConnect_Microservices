package com.hireconnect.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;


@FeignClient(name = "application-service")
public interface ApplicationServiceClient {

    @GetMapping("/applications/job/{jobId}/count")
    Integer getApplicationCountByJob(@PathVariable Long jobId);

    @GetMapping("/applications/recruiter/{recruiterId}/count")
    Long getRecruiterApplicationCount(@PathVariable UUID recruiterId);

    @GetMapping("/applications/recruiter/{recruiterId}/shortlisted/count")
    Integer getShortlistedCount(@PathVariable UUID recruiterId);

    @GetMapping("/applications/recruiter/{recruiterId}/interview-scheduled/count")
    Integer getInterviewScheduledCount(@PathVariable UUID recruiterId);

    @GetMapping("/applications/recruiter/{recruiterId}/offered/count")
    Integer getOfferedCount(@PathVariable UUID recruiterId);

    @GetMapping("/applications/recruiter/{recruiterId}/rejected/count")
    Integer getRejectedCount(@PathVariable UUID recruiterId);

    @GetMapping("/applications/recruiter/{recruiterId}/avg-time-to-hire")
    Double getAverageTimeToHire(@PathVariable UUID recruiterId);

    @GetMapping("/applications/platform/total")
    Long getPlatformApplicationCount();

    @GetMapping("/applications/platform/shortlisted/count")
    Integer getPlatformShortlistedCount();

    @GetMapping("/applications/platform/interview-scheduled/count")
    Integer getPlatformInterviewScheduledCount();

    @GetMapping("/applications/platform/offered/count")
    Integer getPlatformOfferedCount();

    @GetMapping("/applications/platform/rejected/count")
    Integer getPlatformRejectedCount();

    @GetMapping("/applications/platform/avg-time-to-hire")
    Double getPlatformAverageTimeToHire();
    
    @GetMapping("/applications/count/total")
    Integer getAllApplicationsCount();
}