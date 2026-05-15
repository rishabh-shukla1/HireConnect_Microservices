package com.hireconnect.job.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.enums.JobType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long jobId;
    private String title;
    private String category;
    private JobType type;
    private String location;
    private Double minSalary;
    private Double maxSalary;
    private List<String> skills;
    private Integer experienceRequired;
    private UUID postedBy;
    private JobStatus status;
    private LocalDateTime postedAt;
    private String description;
    private String company;
    private String companyName;
    private Long viewCount;
}