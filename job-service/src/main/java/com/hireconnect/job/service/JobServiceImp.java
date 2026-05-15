package com.hireconnect.job.service;

import com.hireconnect.job.client.AnalyticsClient;
import com.hireconnect.job.document.JobDocument;
import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.exception.JobNotFoundException;
import com.hireconnect.job.messaging.JobNotificationProducer;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobSearchRepository;
import com.hireconnect.job.util.JobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the JobService interface.
 * Handles creating, updating, deleting, and searching job postings.
 * Indexes jobs in Elasticsearch for full-text search and sends
 * notifications via RabbitMQ when job status changes.
 */
@Service
@RequiredArgsConstructor
public class JobServiceImp implements JobService {

    private final JobRepository repository;
    private final JobSearchRepository jobSearchRepository;
    private final JobNotificationProducer notificationProducer;
    private final JobMapper jobMapper;

    /**
     * Creates a new job posting. Validates salary range, persists
     * the entity, indexes it in Elasticsearch, and sends a notification.
     *
     * @param request the job creation request DTO
     * @return the created job as a response DTO
     * @throws IllegalArgumentException if minSalary > maxSalary
     */
    @Override
    public JobResponse addJob(JobRequest request) {

        validateSalaryRange(request);

        Job job = jobMapper.toEntity(request);

        Job savedJob = repository.save(job);
    
        try {
            JobDocument doc = toJobDocument(savedJob);
            jobSearchRepository.save(doc);
        } catch (Exception e) {
            System.err.println("Failed to index job in Elasticsearch: " + e.getMessage());
        }

        notificationProducer.sendJobCreated(savedJob.getJobId());

        return jobMapper.toResponse(savedJob);
    }
    
    private JobDocument toJobDocument(Job job) {
        JobDocument doc = new JobDocument();
        doc.setJobId(job.getJobId());
        doc.setTitle(job.getTitle());
        doc.setCategory(job.getCategory());
        doc.setType(job.getType().name());
        doc.setLocation(job.getLocation());
        doc.setMinSalary(job.getMinSalary());
        doc.setMaxSalary(job.getMaxSalary());
        doc.setDescription(job.getDescription());
        doc.setExperienceRequired(job.getExperienceRequired());
        doc.setStatus(job.getStatus().name());
        doc.setPostedBy(job.getPostedBy());
        return doc;
    }

    @Override
    public List<JobResponse> getAllJobs() {
        return repository.findByStatus(JobStatus.OPEN)
                .stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    public JobResponse getJobById(Long jobId) {
        return jobMapper.toResponse(findJobById(jobId));
    }

    @Override
    public JobResponse updateJob(Long jobId, JobRequest request) {

        validateSalaryRange(request);

        Job job = findJobById(jobId);

        job.setTitle(request.getTitle());
        job.setCategory(request.getCategory());
        job.setType(request.getType());
        job.setLocation(request.getLocation());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        job.setDescription(request.getDescription());
        job.setSkills(request.getSkills());
        job.setExperienceRequired(request.getExperienceRequired());

        if (!job.getPostedBy().equals(request.getPostedBy())) {
            throw new IllegalArgumentException("You are not allowed to update another recruiter's job");
        }

        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        Job updatedJob = repository.save(job);

        return jobMapper.toResponse(updatedJob);
    }

    /**
     * Soft-deletes a job by setting its status to DELETED.
     * Notifies downstream consumers of the status change.
     *
     * @param jobId the ID of the job to delete
     * @throws JobNotFoundException if the job does not exist
     */
    @Override
    public void deleteJob(Long jobId) {

        Job job = findJobById(jobId);

        job.setStatus(JobStatus.DELETED);

        repository.save(job);

        notificationProducer.sendJobStatusChanged(jobId, JobStatus.DELETED.name());
    }

    @Override
    public List<JobResponse> getJobsByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category)
                .stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    public List<JobResponse> getJobsByLocation(String location) {
        return repository.findByLocationIgnoreCase(location)
                .stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    public List<JobResponse> getJobsByRecruiter(UUID recruiterId) {
        return repository.findByPostedBy(recruiterId)
                .stream()
                .filter(job -> job.getStatus() != JobStatus.DELETED)
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    public List<JobResponse> searchJobs(
            String title,
            String location,
            String category,
            Double minSalary,
            Double maxSalary,
            Integer experience) {

        List<JobDocument> jobDocs;
        
        try {
            if (title != null && !title.trim().isEmpty()) {
                jobDocs = jobSearchRepository.searchByKeyword(title);
            } else {
                jobDocs = jobSearchRepository.findByStatus("OPEN");
            }
            
            return jobDocs.stream()
                    .filter(doc -> doc.getStatus().equals("OPEN"))
                    .filter(doc -> location == null || location.isEmpty() || 
                            doc.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .filter(doc -> category == null || category.isEmpty() || 
                            doc.getCategory().equalsIgnoreCase(category))
                    .filter(doc -> minSalary == null || doc.getMaxSalary() >= minSalary)
                    .filter(doc -> maxSalary == null || doc.getMinSalary() <= maxSalary)
                    .filter(doc -> experience == null || doc.getExperienceRequired() <= experience)
                    .map(this::toJobResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            List<Job> jobs;
            if (title != null && !title.trim().isEmpty()) {
                jobs = repository.findByTitleContainingIgnoreCase(title);
            } else {
                jobs = repository.findByStatus(JobStatus.OPEN);
            }
            
            return jobs.stream()
                    .filter(job -> job.getStatus() == JobStatus.OPEN)
                    .filter(job -> location == null || location.isEmpty() || 
                            job.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .filter(job -> category == null || category.isEmpty() || 
                            job.getCategory().equalsIgnoreCase(category))
                    .filter(job -> minSalary == null || job.getMaxSalary() >= minSalary)
                    .filter(job -> maxSalary == null || job.getMinSalary() <= maxSalary)
                    .filter(job -> experience == null || job.getExperienceRequired() <= experience)
                    .map(jobMapper::toResponse)
                    .toList();
        }
    }
    
    private JobResponse toJobResponse(JobDocument doc) {
        return JobResponse.builder()
                .jobId(doc.getJobId())
                .title(doc.getTitle())
                .category(doc.getCategory())
                .type(doc.getType() != null ? com.hireconnect.job.enums.JobType.valueOf(doc.getType()) : null)
                .location(doc.getLocation())
                .minSalary(doc.getMinSalary())
                .maxSalary(doc.getMaxSalary())
                .description(doc.getDescription())
                .experienceRequired(doc.getExperienceRequired())
                .status(doc.getStatus() != null ? com.hireconnect.job.enums.JobStatus.valueOf(doc.getStatus()) : null)
                .build();
    }

    @Override
    public JobResponse changeStatus(Long jobId, String status) {
        Job job = findJobById(jobId);

        try {
            job.setStatus(JobStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: OPEN, CLOSED, PAUSED, DELETED");
        }

        Job updatedJob = repository.save(job);

        notificationProducer.sendJobStatusChanged(
                updatedJob.getJobId(),
                updatedJob.getStatus().name());

        return jobMapper.toResponse(updatedJob);
    }

    @Override
    public Long getJobViewCount(Long jobId) {

        Job job = repository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with id: " + jobId));

        return job.getViewCount() == null ? 0L : job.getViewCount();
    }

    @Override
    public Long getRecruiterJobCount(UUID recruiterId) {
        return repository.countByPostedBy(recruiterId);
    }

    @Override
    public Map<String, Long> getTopCategories() {

        Map<String, Long> topCategories = new LinkedHashMap<>();

        List<String> categories = repository.findAll()
                .stream()
                .map(Job::getCategory)
                .distinct()
                .toList();

        for (String category : categories) {
            topCategories.put(category, repository.countByCategoryIgnoreCase(category));
        }

        return topCategories;
    }

    private Job findJobById(Long jobId) {
        return repository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with id: " + jobId));
    }

    private void validateSalaryRange(JobRequest request) {
        if (request.getMinSalary() > request.getMaxSalary()) {
            throw new IllegalArgumentException(
                    "Minimum salary cannot be greater than maximum salary");
        }
    }

    @Override
    public Long getRecruiterJobCountByStatus(UUID recruiterId, JobStatus status) {
        return repository.countByPostedByAndStatus(recruiterId, status);
    }
}