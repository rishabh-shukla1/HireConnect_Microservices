package com.hireconnect.application.service;

import com.hireconnect.application.client.NotificationClient;
import com.hireconnect.application.dto.EmailRequest;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.enums.ApplicationStatus;
import com.hireconnect.application.exception.ApplicationNotFoundException;
import com.hireconnect.application.exception.DuplicateApplicationException;
import com.hireconnect.application.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationServiceImp.
 * Covers the core application lifecycle: submit, status transitions,
 * withdrawal, duplicate detection, and count queries.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceImpTest {

    /** Mocked JPA repository for Application persistence. */
    @Mock
    private ApplicationRepository applicationRepository;

    /** Mocked RabbitMQ template (interface) for publishing application events. */
    @Mock
    private AmqpTemplate rabbitTemplate;

    /** Mocked Feign client used to send notification emails. */
    @Mock
    private NotificationClient notificationClient;

    /** The service under test — uses the mocks above. */
    @InjectMocks
    private ApplicationServiceImp applicationService;

    // ─── Submit Application ─────────────────────────────────────

    /**
     * Verifies a new application is saved with status APPLIED,
     * an email notification is sent, and an event is published.
     */
    @Test
    void submitApplication_ShouldSaveAndNotify() {

        UUID candidateId = UUID.randomUUID();
        Long jobId = 1L;

        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setJobId(jobId);
        application.setRecruiterId(UUID.randomUUID());
        application.setCandidateEmail("candidate@test.com");
        application.setJobTitle("Java Developer");

        // No existing application for this candidate-job pair
        when(applicationRepository.findFirstByJobIdAndCandidateId(jobId, candidateId))
                .thenReturn(Optional.empty());
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        Application result = applicationService.submitApplication(application);

        // Status should be set to APPLIED by the service
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
        assertNotNull(result.getAppliedAt());

        // Verify email notification was triggered
        verify(notificationClient).sendEmail(any(EmailRequest.class));

        // Verify RabbitMQ event was published
        verify(rabbitTemplate).convertAndSend(
                eq("application.exchange"),
                eq("application.submitted"),
                any(Object.class)
        );
    }

    /**
     * Verifies that submitting a duplicate application
     * (same candidate + same job) throws DuplicateApplicationException.
     */
    @Test
    void submitApplication_Duplicate_ShouldThrow() {

        UUID candidateId = UUID.randomUUID();
        Long jobId = 1L;

        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setJobId(jobId);

        // Simulate an existing application already in the database
        when(applicationRepository.findFirstByJobIdAndCandidateId(jobId, candidateId))
                .thenReturn(Optional.of(application));

        assertThrows(DuplicateApplicationException.class,
                () -> applicationService.submitApplication(application));

        // Should never reach save or notification
        verify(applicationRepository, never()).save(any());
        verify(notificationClient, never()).sendEmail(any());
    }

    // ─── Status Updates ─────────────────────────────────────────

    /**
     * Verifies updating status to SHORTLISTED persists the change,
     * sends a notification email, and publishes a status-changed event.
     */
    @Test
    void updateStatus_Shortlisted_ShouldUpdateAndNotify() {

        UUID applicationId = UUID.randomUUID();

        Application application = new Application();
        application.setApplicationId(applicationId);
        application.setCandidateEmail("candidate@test.com");
        application.setJobTitle("Java Developer");
        application.setStatus(ApplicationStatus.APPLIED);
        application.setRecruiterId(UUID.randomUUID());
        application.setCandidateId(UUID.randomUUID());
        application.setJobId(1L);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        Application result = applicationService.updateStatus(applicationId, ApplicationStatus.SHORTLISTED);

        assertEquals(ApplicationStatus.SHORTLISTED, result.getStatus());
        verify(notificationClient).sendEmail(any(EmailRequest.class));
        verify(rabbitTemplate).convertAndSend(
                eq("application.exchange"),
                eq("application.status.updated"),
                any(Object.class)
        );
    }

    // ─── Get By Id ──────────────────────────────────────────────

    /**
     * Verifies getById returns the correct application when found.
     */
    @Test
    void getById_Found_ShouldReturnApplication() {

        UUID applicationId = UUID.randomUUID();

        Application application = new Application();
        application.setApplicationId(applicationId);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        Application result = applicationService.getById(applicationId);

        assertNotNull(result);
        assertEquals(applicationId, result.getApplicationId());
    }

    /**
     * Verifies getById throws ApplicationNotFoundException
     * when the application does not exist.
     */
    @Test
    void getById_NotFound_ShouldThrow() {

        UUID applicationId = UUID.randomUUID();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> applicationService.getById(applicationId));
    }

    // ─── Withdraw ───────────────────────────────────────────────

    /**
     * Verifies that withdrawing sets status to WITHDRAW,
     * sends a notification, and publishes an event.
     */
    @Test
    void withdrawApplication_ShouldSetWithdrawAndNotify() {

        UUID applicationId = UUID.randomUUID();

        Application application = new Application();
        application.setApplicationId(applicationId);
        application.setCandidateEmail("candidate@test.com");
        application.setJobTitle("Java Developer");
        application.setStatus(ApplicationStatus.APPLIED);
        application.setRecruiterId(UUID.randomUUID());
        application.setCandidateId(UUID.randomUUID());
        application.setJobId(1L);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        applicationService.withdrawApplication(applicationId);

        assertEquals(ApplicationStatus.WITHDRAW, application.getStatus());
        verify(notificationClient).sendEmail(any(EmailRequest.class));
        verify(rabbitTemplate).convertAndSend(
                eq("application.exchange"),
                eq("application.withdrawn"),
                any(Object.class)
        );
    }

    // ─── Count Queries ──────────────────────────────────────────

    /**
     * Verifies countByJob delegates to the repository correctly.
     */
    @Test
    void countByJob_ShouldReturnCount() {

        when(applicationRepository.countByJobId(1L)).thenReturn(5L);

        long count = applicationService.countByJob(1L);

        assertEquals(5L, count);
    }

    /**
     * Verifies getByCandidate returns the list from the repository.
     */
    @Test
    void getByCandidate_ShouldReturnList() {

        UUID candidateId = UUID.randomUUID();
        Application app = new Application();
        app.setCandidateId(candidateId);

        when(applicationRepository.findByCandidateId(candidateId)).thenReturn(List.of(app));

        List<Application> result = applicationService.getByCandidate(candidateId);

        assertEquals(1, result.size());
    }
}
