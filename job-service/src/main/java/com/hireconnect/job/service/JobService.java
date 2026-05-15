package com.hireconnect.job.service;

import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.enums.JobStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JobService {

    JobResponse addJob(JobRequest request);

    List<JobResponse> getAllJobs();

    JobResponse getJobById(Long jobId);

    JobResponse updateJob(Long jobId, JobRequest request);

    void deleteJob(Long jobId);

    List<JobResponse> getJobsByCategory(String category);

    List<JobResponse> getJobsByLocation(String location);

    List<JobResponse> getJobsByRecruiter(UUID recruiterId);

    List<JobResponse> searchJobs(
            String title,
            String location,
            String category,
            Double minSalary,
            Double maxSalary,
            Integer experience
    );

    JobResponse changeStatus(Long jobId, String status);

    Long getJobViewCount(Long jobId);

    Long getRecruiterJobCount(UUID recruiterId);

    Map<String, Long> getTopCategories();

    Long getRecruiterJobCountByStatus(UUID recruiterId, JobStatus status);
}