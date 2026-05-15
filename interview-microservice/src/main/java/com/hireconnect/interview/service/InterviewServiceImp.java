package com.hireconnect.interview.service;

import com.hireconnect.interview.client.ApplicationServiceClient;
import com.hireconnect.interview.client.NotificationClient;
import com.hireconnect.interview.dto.EmailRequest;
import com.hireconnect.interview.dto.StatusUpdateRequest;
import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.enums.ApplicationStatus;
import com.hireconnect.interview.enums.InterviewStatus;
import com.hireconnect.interview.event.InterviewNotificationEvent;
import com.hireconnect.interview.exception.InterviewAlreadyExistsException;
import com.hireconnect.interview.exception.InterviewNotFoundException;
import com.hireconnect.interview.repository.InterviewRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the InterviewService interface.
 * Manages the full interview lifecycle — scheduling, confirming,
 * rescheduling, and cancelling. Coordinates with the application-service
 * (via Feign) and sends email alerts through the notification-service.
 */
@Service
public class InterviewServiceImp implements InterviewService {

    private final InterviewRepository repository;
    private final AmqpTemplate rabbitTemplate;
    private final ApplicationServiceClient applicationServiceClient;
    private final NotificationClient notificationClient;

    public InterviewServiceImp(InterviewRepository repository, AmqpTemplate rabbitTemplate, ApplicationServiceClient applicationServiceClient, NotificationClient notificationClient) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.applicationServiceClient = applicationServiceClient;
        this.notificationClient = notificationClient;
    }

    /**
     * Schedules a new interview for an application.
     * Guards against duplicate interviews and recruiter time-slot conflicts.
     * Advances the application status to INTERVIEW_SCHEDULED and sends
     * an email notification to the candidate.
     *
     * @param interview the interview entity to schedule
     * @return the persisted interview with generated ID and SCHEDULED status
     * @throws InterviewAlreadyExistsException if a duplicate or slot conflict exists
     */
    @Override
    public Interview scheduleInterview(Interview interview) {

        if (repository.existsByApplicationId(interview.getApplicationId())) {
            throw new InterviewAlreadyExistsException(
                    "Interview already exists for this application"
            );
        }

        boolean slotTaken = repository.existsByScheduledAtAndRecruiterIdAndStatusNot(
                interview.getScheduledAt(),
                interview.getRecruiterId(),
                InterviewStatus.CANCELLED
        );

        if (slotTaken) {
            throw new InterviewAlreadyExistsException(
                    "Recruiter already has an interview scheduled at this time"
            );
        }

        interview.setInterviewId(UUID.randomUUID());
        interview.setStatus(InterviewStatus.SCHEDULED);

        Interview saved = repository.save(interview);

        applicationServiceClient.updateStatus(
                saved.getApplicationId(),
                new StatusUpdateRequest(ApplicationStatus.INTERVIEW_SCHEDULED)
        );

        notificationClient.sendEmail( new EmailRequest(
                saved.getCandidateEmail(),
                "Interview Scheduled",
                "Your interview has been scheduled.\n\n"
                        + "Date & Time: " + saved.getScheduledAt() + "\n"
                        + "Mode: " + saved.getMode() + "\n\n"
                        + "Best of luck. Go cook, respectfully."
        ));

        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "interview.scheduled",
                new InterviewNotificationEvent(
                        saved.getInterviewId(),
                        saved.getApplicationId(),
                        saved.getCandidateId(),
                        saved.getRecruiterId(),
                        saved.getScheduledAt(),
                        saved.getStatus(),
                        "Interview scheduled for " + saved.getScheduledAt()
                )
        );

        return saved;
    }

    /**
     * Confirms a previously scheduled interview.
     * Sets status to CONFIRMED and notifies the candidate.
     *
     * @param interviewId the UUID of the interview to confirm
     * @return the updated interview entity
     */
    @Override
    public Interview confirmInterview(UUID interviewId) {

        Interview interview = getById(interviewId);

        interview.setStatus(InterviewStatus.CONFIRMED);

        Interview saved = repository.save(interview);

        notificationClient.sendEmail( new EmailRequest(
                saved.getCandidateEmail(),
                "Interview Confirmed",
                "You confirmed your interview scheduled for "
                        + saved.getScheduledAt()
        ));

        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "interview.confirmed",
                new InterviewNotificationEvent(
                        saved.getInterviewId(),
                        saved.getApplicationId(),
                        saved.getCandidateId(),
                        saved.getRecruiterId(),
                        saved.getScheduledAt(),
                        saved.getStatus(),
                        "Interview confirmed"
                )
        );

        return saved;
    }

    /**
     * Reschedules an interview to a new date/time.
     * Checks for recruiter slot conflicts at the new time.
     *
     * @param interviewId the UUID of the interview to reschedule
     * @param newDateTime the new date and time for the interview
     * @return the updated interview entity
     * @throws InterviewAlreadyExistsException if the new slot is taken
     */
    @Override
    public Interview rescheduleInterview(UUID interviewId, LocalDateTime newDateTime) {

        Interview interview = getById(interviewId);

        boolean slotTaken = repository.existsByScheduledAtAndRecruiterIdAndStatusNot(
                newDateTime,
                interview.getRecruiterId(),
                InterviewStatus.CANCELLED
        );

        if (slotTaken) {
            throw new InterviewAlreadyExistsException(
                    "Recruiter already has an interview scheduled at this time"
            );
        }

        interview.setScheduledAt(newDateTime);
        interview.setStatus(InterviewStatus.RESCHEDULED);

        Interview saved = repository.save(interview);

        notificationClient.sendEmail(new EmailRequest(
                saved.getCandidateEmail(),
                "Interview Rescheduled",
                "Your interview has been rescheduled to "
                        + saved.getScheduledAt()
        ));

        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "interview.rescheduled",
                new InterviewNotificationEvent(
                        saved.getInterviewId(),
                        saved.getApplicationId(),
                        saved.getCandidateId(),
                        saved.getRecruiterId(),
                        saved.getScheduledAt(),
                        saved.getStatus(),
                        "Interview rescheduled to " + saved.getScheduledAt()
                )
        );

        return saved;
    }

    /**
     * Cancels an interview and sends a cancellation notification.
     *
     * @param interviewId the UUID of the interview to cancel
     */
    @Override
    public void cancelInterview(UUID interviewId) {

        Interview interview = getById(interviewId);

        interview.setStatus(InterviewStatus.CANCELLED);

        Interview saved = repository.save(interview);

        notificationClient.sendEmail( new EmailRequest(
                saved.getCandidateEmail(),
                "Interview Cancelled",
                "Your interview scheduled for "
                        + saved.getScheduledAt()
                        + " has been cancelled."
        ));

        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "interview.cancelled",
                new InterviewNotificationEvent(
                        saved.getInterviewId(),
                        saved.getApplicationId(),
                        saved.getCandidateId(),
                        saved.getRecruiterId(),
                        saved.getScheduledAt(),
                        saved.getStatus(),
                        "Interview cancelled"
                )
        );
    }

    @Override
    public List<Interview> getByCandidate(UUID candidateId) {
        return repository.findByCandidateId(candidateId);
    }

    @Override
    public List<Interview> getByRecruiter(UUID recruiterId) {
        return repository.findByRecruiterId(recruiterId);
    }

    @Override
    public List<Interview> getByApplication(UUID applicationId) {
        return repository.findByApplicationId(applicationId);
    }

    @Override
    public List<Interview> getByStatus(InterviewStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public Interview getById(UUID interviewId) {
        return repository.findByInterviewId(interviewId)
                .orElseThrow(() ->
                        new InterviewNotFoundException(
                                "Interview not found with id: " + interviewId
                        ));
    }

    @Override
    public List<Interview> getByCandidateEmail(String email) {
        return repository.findByCandidateEmail(email);
    }
}
