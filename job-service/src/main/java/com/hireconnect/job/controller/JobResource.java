package com.hireconnect.job.controller;

import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Validated
public class JobResource {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> addJob(@Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.addJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<JobResponse>> getJobsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(jobService.getJobsByCategory(category));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<JobResponse>> getJobsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(jobService.getJobsByLocation(location));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobResponse>> getJobsByRecruiter(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponse>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Integer experience) {

        return ResponseEntity.ok(
                jobService.searchJobs(
                        title,
                        location,
                        category,
                        minSalary,
                        maxSalary,
                        experience));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request) {

        return ResponseEntity.ok(jobService.updateJob(jobId, request));
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<JobResponse> changeJobStatus(
            @PathVariable Long jobId,
            @RequestParam JobStatus status) {

        return ResponseEntity.ok(jobService.changeStatus(jobId, status.name()));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{jobId}/views")
    public ResponseEntity<Long> getJobViewCount(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobViewCount(jobId));
    }

    

    @GetMapping("/recruiter/{recruiterId}/count")
    public ResponseEntity<Long> getRecruiterJobCount(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(jobService.getRecruiterJobCount(recruiterId));
    }

    @GetMapping("/categories/top")
    public ResponseEntity<Map<String, Long>> getTopCategories() {
        return ResponseEntity.ok(jobService.getTopCategories());
    }

    @GetMapping("/recruiter/{recruiterId}/active/count")
    public ResponseEntity<Long> getActiveJobCount(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(jobService.getRecruiterJobCountByStatus(recruiterId, JobStatus.OPEN));
    }

    @GetMapping("/recruiter/{recruiterId}/closed/count")
    public ResponseEntity<Long> getClosedJobCount(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(jobService.getRecruiterJobCountByStatus(recruiterId, JobStatus.CLOSED));
    }
}