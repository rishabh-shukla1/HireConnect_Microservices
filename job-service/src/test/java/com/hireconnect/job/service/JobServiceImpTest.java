package com.hireconnect.job.service;

import com.hireconnect.job.client.AnalyticsClient;
import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.enums.JobType;
import com.hireconnect.job.messaging.JobNotificationProducer;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobSearchRepository;
import com.hireconnect.job.util.JobMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobServiceImp.
 * Covers adding, retrieving, updating, and deleting job postings,
 * verifying interactions with the repository, mapper, and notification producer.
 */
@ExtendWith(MockitoExtension.class)
class JobServiceImpTest {

    @Mock
    private JobRepository repository;

    @Mock
    private JobSearchRepository jobSearchRepository;

    @Mock
    private AnalyticsClient analyticsClient;

    @Mock
    private JobNotificationProducer notificationProducer;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImp jobService;

    /**
     * Verifies that adding a valid job maps the request to an entity,
     * saves it, indexes it in Elasticsearch, and sends a notification.
     */
    @Test
    void addJob_ShouldReturnSavedJob() {

        UUID recruiterId = UUID.randomUUID();

        JobRequest request = new JobRequest();
        request.setTitle("Java Developer");
        request.setCategory("IT");
        request.setType(JobType.FULL_TIME);
        request.setLocation("Delhi");
        request.setMinSalary(50000.0);
        request.setMaxSalary(100000.0);
        request.setSkills(List.of("Java", "Spring Boot"));
        request.setExperienceRequired(2);
        request.setPostedBy(recruiterId);
        request.setDescription("Looking for an experienced Java developer.");

        Job job = Job.builder()
                .jobId(1L)
                .title("Java Developer")
                .category("IT")
                .type(JobType.FULL_TIME)
                .location("Delhi")
                .minSalary(50000.0)
                .maxSalary(100000.0)
                .skills(List.of("Java", "Spring Boot"))
                .experienceRequired(2)
                .postedBy(recruiterId)
                .description("Looking for an experienced Java developer.")
                .status(JobStatus.OPEN)
                .build();

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Java Developer")
                .status(JobStatus.OPEN)
                .build();

        when(jobMapper.toEntity(request)).thenReturn(job);
        when(repository.save(job)).thenReturn(job);
        when(jobMapper.toResponse(job)).thenReturn(response);

        JobResponse result = jobService.addJob(request);

        assertEquals(1L, result.getJobId());
        assertEquals("Java Developer", result.getTitle());

        verify(notificationProducer).sendJobCreated(1L);
    }

    /**
     * Verifies that getJobById retrieves and maps the job entity correctly.
     */
    @Test
    void getJobById_ShouldReturnJob() {

        Job job = Job.builder()
                .jobId(1L)
                .title("Java Developer")
                .status(JobStatus.OPEN)
                .build();

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Java Developer")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(job));
        when(jobMapper.toResponse(job)).thenReturn(response);

        JobResponse result = jobService.getJobById(1L);

        assertEquals("Java Developer", result.getTitle());
    }

    /**
     * Verifies that getAllJobs returns only OPEN jobs mapped to responses.
     */
    @Test
    void getAllJobs_ShouldReturnList() {

        Job job = Job.builder()
                .jobId(1L)
                .title("Java Developer")
                .status(JobStatus.OPEN)
                .build();

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Java Developer")
                .build();

        when(repository.findByStatus(JobStatus.OPEN))
                .thenReturn(List.of(job));

        when(jobMapper.toResponse(job)).thenReturn(response);

        List<JobResponse> result = jobService.getAllJobs();

        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).getTitle());
    }

    /**
     * Verifies that updateJob persists the new title and details
     * when the recruiter owns the job.
     */
    @Test
    void updateJob_ShouldReturnUpdatedJob() {

        UUID recruiterId = UUID.randomUUID();

        Job existingJob = Job.builder()
                .jobId(1L)
                .postedBy(recruiterId)
                .status(JobStatus.OPEN)
                .build();

        JobRequest request = new JobRequest();
        request.setTitle("Senior Java Developer");
        request.setCategory("IT");
        request.setType(JobType.FULL_TIME);
        request.setLocation("Delhi");
        request.setMinSalary(70000.0);
        request.setMaxSalary(120000.0);
        request.setSkills(List.of("Java", "Spring"));
        request.setExperienceRequired(3);
        request.setPostedBy(recruiterId);
        request.setDescription("Updated description");

        Job updatedJob = Job.builder()
                .jobId(1L)
                .title("Senior Java Developer")
                .postedBy(recruiterId)
                .status(JobStatus.OPEN)
                .build();

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Senior Java Developer")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(repository.save(any(Job.class))).thenReturn(updatedJob);
        when(jobMapper.toResponse(updatedJob)).thenReturn(response);

        JobResponse result = jobService.updateJob(1L, request);

        assertEquals("Senior Java Developer", result.getTitle());
    }

    /**
     * Verifies that deleteJob soft-deletes the job by setting
     * status to DELETED and notifies downstream consumers.
     */
    @Test
    void deleteJob_ShouldMarkAsDeleted() {

        Job job = Job.builder()
                .jobId(1L)
                .status(JobStatus.OPEN)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(job));

        jobService.deleteJob(1L);

        assertEquals(JobStatus.DELETED, job.getStatus());

        verify(repository).save(job);
        verify(notificationProducer)
                .sendJobStatusChanged(1L, "DELETED");
    }
}