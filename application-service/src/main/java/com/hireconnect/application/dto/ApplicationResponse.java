package com.hireconnect.application.dto;

import com.hireconnect.application.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponse {

    private UUID applicationId;
    private Long jobId;
    private UUID candidateId;
    private UUID recruiterId;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    private ApplicationStatus status;

    private String coverLetter;
    private String resumeUrl;

    // Enriched fields (populated at the controller from job / profile services
    // so the UI doesn't have to do N+1 fetches).
    private String jobTitle;
    private String companyName;
    private String location;
    private String jobType;
    private List<String> skills;

    private String candidateName;
    private String candidateEmail;
}