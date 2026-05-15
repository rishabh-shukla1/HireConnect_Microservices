package com.hireconnect.job.util;

import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(JobRequest request) {
        return Job.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .type(request.getType())
                .location(request.getLocation())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .description(request.getDescription())
                .skills(request.getSkills())
                .experienceRequired(request.getExperienceRequired())
                .postedBy(request.getPostedBy())
                .status(request.getStatus())
                .build();
    }

    public JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .jobId(job.getJobId())
                .title(job.getTitle())
                .category(job.getCategory())
                .type(job.getType())
                .location(job.getLocation())
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .description(job.getDescription())
                .skills(job.getSkills())
                .experienceRequired(job.getExperienceRequired())
                .postedBy(job.getPostedBy())
                .status(job.getStatus())
                .postedAt(job.getPostedAt())
                .company(job.getCompany())
                .companyName(job.getCompany())
                .viewCount(job.getViewCount())
                .build();
    }
}