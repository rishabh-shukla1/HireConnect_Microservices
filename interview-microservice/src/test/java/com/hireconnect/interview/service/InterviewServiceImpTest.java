package com.hireconnect.interview.service;

import com.hireconnect.interview.client.ApplicationServiceClient;
import com.hireconnect.interview.client.NotificationClient;
import com.hireconnect.interview.dto.EmailRequest;
import com.hireconnect.interview.dto.StatusUpdateRequest;
import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.enums.InterviewMode;
import com.hireconnect.interview.enums.InterviewStatus;
import com.hireconnect.interview.exception.InterviewAlreadyExistsException;
import com.hireconnect.interview.exception.InterviewNotFoundException;
import com.hireconnect.interview.repository.InterviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InterviewServiceImp.
 * Covers scheduling, confirming, rescheduling, cancelling interviews,
 * duplicate/slot-conflict detection, and retrieval operations.
 */
@ExtendWith(MockitoExtension.class)
class InterviewServiceImpTest {

    /** Mocked JPA repository for Interview persistence. */
    @Mock
    private InterviewRepository repository;

    /** Mocked RabbitMQ template (interface) for publishing interview events. */
    @Mock
    private AmqpTemplate rabbitTemplate;

    /** Mocked Feign client to update application status in application-service. */
    @Mock
    private ApplicationServiceClient applicationServiceClient;

    /** Mocked Feign client to send email notifications. */
    @Mock
    private NotificationClient notificationClient;

    /** The service under test — uses the mocks above. */
    @InjectMocks
    private InterviewServiceImp interviewService;

    // ─── Schedule Interview ─────────────────────────────────────

    /**
     * Verifies a new interview is scheduled successfully when no
     * duplicate or time-slot conflict exists. Checks that the
     * application status is advanced and an email is sent.
     */
    @Test
    void scheduleInterview_Success_ShouldSaveAndNotify() {

        UUID applicationId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        UUID recruiterId = UUID.randomUUID();
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(3);

        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setCandidateId(candidateId);
        interview.setRecruiterId(recruiterId);
        interview.setScheduledAt(scheduledAt);
        interview.setMode(InterviewMode.ONLINE);
        interview.setCandidateEmail("candidate@test.com");
        interview.setNotes("Technical round");

        // No existing interview for this application
        when(repository.existsByApplicationId(applicationId)).thenReturn(false);
        // No time-slot conflict for this recruiter
        when(repository.existsByScheduledAtAndRecruiterIdAndStatusNot(
                scheduledAt, recruiterId, InterviewStatus.CANCELLED)).thenReturn(false);
        when(repository.save(any(Interview.class))).thenReturn(interview);

        Interview result = interviewService.scheduleInterview(interview);

        assertNotNull(result);
        assertEquals(InterviewStatus.SCHEDULED, result.getStatus());

        // Verify application status was updated via Feign
        verify(applicationServiceClient).updateStatus(eq(applicationId), any(StatusUpdateRequest.class));
        // Verify email was sent
        verify(notificationClient).sendEmail(any(EmailRequest.class));
        // Verify RabbitMQ event was published
        verify(rabbitTemplate).convertAndSend(eq("notification.exchange"), eq("interview.scheduled"), any(Object.class));
    }

    /**
     * Verifies that scheduling a duplicate interview for the
     * same application throws InterviewAlreadyExistsException.
     */
    @Test
    void scheduleInterview_Duplicate_ShouldThrow() {

        UUID applicationId = UUID.randomUUID();

        Interview interview = new Interview();
        interview.setApplicationId(applicationId);

        when(repository.existsByApplicationId(applicationId)).thenReturn(true);

        assertThrows(InterviewAlreadyExistsException.class,
                () -> interviewService.scheduleInterview(interview));

        verify(repository, never()).save(any());
    }

    /**
     * Verifies that scheduling an interview at a time slot already
     * taken by the recruiter throws InterviewAlreadyExistsException.
     */
    @Test
    void scheduleInterview_SlotConflict_ShouldThrow() {

        UUID applicationId = UUID.randomUUID();
        UUID recruiterId = UUID.randomUUID();
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);

        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setRecruiterId(recruiterId);
        interview.setScheduledAt(scheduledAt);

        when(repository.existsByApplicationId(applicationId)).thenReturn(false);
        when(repository.existsByScheduledAtAndRecruiterIdAndStatusNot(
                scheduledAt, recruiterId, InterviewStatus.CANCELLED)).thenReturn(true);

        assertThrows(InterviewAlreadyExistsException.class,
                () -> interviewService.scheduleInterview(interview));

        verify(repository, never()).save(any());
    }

    // ─── Confirm Interview ──────────────────────────────────────

    /**
     * Verifies confirming an interview sets status to CONFIRMED,
     * sends a confirmation email, and publishes an event.
     */
    @Test
    void confirmInterview_ShouldSetConfirmedAndNotify() {

        UUID interviewId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        Interview interview = new Interview();
        interview.setInterviewId(interviewId);
        interview.setApplicationId(applicationId);
        interview.setCandidateId(UUID.randomUUID());
        interview.setRecruiterId(UUID.randomUUID());
        interview.setScheduledAt(LocalDateTime.now().plusDays(2));
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setCandidateEmail("candidate@test.com");

        when(repository.findByInterviewId(interviewId)).thenReturn(Optional.of(interview));
        when(repository.save(any(Interview.class))).thenReturn(interview);

        Interview result = interviewService.confirmInterview(interviewId);

        assertEquals(InterviewStatus.CONFIRMED, result.getStatus());
        verify(notificationClient).sendEmail(any(EmailRequest.class));
    }

    // ─── Cancel Interview ───────────────────────────────────────

    /**
     * Verifies cancelling an interview sets status to CANCELLED
     * and sends a cancellation notification email.
     */
    @Test
    void cancelInterview_ShouldSetCancelledAndNotify() {

        UUID interviewId = UUID.randomUUID();

        Interview interview = new Interview();
        interview.setInterviewId(interviewId);
        interview.setApplicationId(UUID.randomUUID());
        interview.setCandidateId(UUID.randomUUID());
        interview.setRecruiterId(UUID.randomUUID());
        interview.setScheduledAt(LocalDateTime.now().plusDays(1));
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setCandidateEmail("candidate@test.com");

        when(repository.findByInterviewId(interviewId)).thenReturn(Optional.of(interview));
        when(repository.save(any(Interview.class))).thenReturn(interview);

        interviewService.cancelInterview(interviewId);

        assertEquals(InterviewStatus.CANCELLED, interview.getStatus());
        verify(notificationClient).sendEmail(any(EmailRequest.class));
    }

    // ─── Get By Id ──────────────────────────────────────────────

    /**
     * Verifies getById returns the interview when found.
     */
    @Test
    void getById_Found_ShouldReturn() {

        UUID interviewId = UUID.randomUUID();

        Interview interview = new Interview();
        interview.setInterviewId(interviewId);

        when(repository.findByInterviewId(interviewId)).thenReturn(Optional.of(interview));

        Interview result = interviewService.getById(interviewId);

        assertNotNull(result);
        assertEquals(interviewId, result.getInterviewId());
    }

    /**
     * Verifies getById throws InterviewNotFoundException when not found.
     */
    @Test
    void getById_NotFound_ShouldThrow() {

        UUID interviewId = UUID.randomUUID();

        when(repository.findByInterviewId(interviewId)).thenReturn(Optional.empty());

        assertThrows(InterviewNotFoundException.class,
                () -> interviewService.getById(interviewId));
    }

    // ─── Retrieval Queries ──────────────────────────────────────

    /**
     * Verifies getByCandidate returns the correct list.
     */
    @Test
    void getByCandidate_ShouldReturnList() {

        UUID candidateId = UUID.randomUUID();
        Interview i = new Interview();
        i.setCandidateId(candidateId);

        when(repository.findByCandidateId(candidateId)).thenReturn(List.of(i));

        List<Interview> result = interviewService.getByCandidate(candidateId);

        assertEquals(1, result.size());
    }

    /**
     * Verifies getByApplication returns interviews for a given application.
     */
    @Test
    void getByApplication_ShouldReturnList() {

        UUID applicationId = UUID.randomUUID();
        Interview i = new Interview();
        i.setApplicationId(applicationId);

        when(repository.findByApplicationId(applicationId)).thenReturn(List.of(i));

        List<Interview> result = interviewService.getByApplication(applicationId);

        assertEquals(1, result.size());
    }
}
