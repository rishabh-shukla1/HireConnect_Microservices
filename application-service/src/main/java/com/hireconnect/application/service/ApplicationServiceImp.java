package com.hireconnect.application.service;

import com.hireconnect.application.client.NotificationClient;
import com.hireconnect.application.dto.EmailRequest;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.enums.ApplicationStatus;
import com.hireconnect.application.event.ApplicationStatusChangedEvent;
import com.hireconnect.application.exception.ApplicationNotFoundException;
import com.hireconnect.application.exception.DuplicateApplicationException;
import com.hireconnect.application.repository.ApplicationRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the ApplicationService interface.
 * Manages the full lifecycle of job applications — from submission
 * through status transitions (shortlist, interview, offer, reject)
 * to withdrawal. Sends email notifications and publishes RabbitMQ
 * events for each significant state change.
 */
@Service
public class ApplicationServiceImp implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AmqpTemplate rabbitTemplate;
    private final NotificationClient notificationClient;

    public ApplicationServiceImp(ApplicationRepository applicationRepository,
                                 AmqpTemplate rabbitTemplate, NotificationClient notificationClient) {
        this.applicationRepository = applicationRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.notificationClient = notificationClient;

    }

    /**
     * Submits a new application after checking for duplicates.
     * Sets the initial status to APPLIED, sends a confirmation email
     * to the candidate, and publishes an event to RabbitMQ.
     *
     * @param application the application entity to submit
     * @return the persisted application with generated ID and timestamps
     * @throws DuplicateApplicationException if the candidate already applied for this job
     */
    @Override
    public Application submitApplication(Application application) {

        applicationRepository
                .findFirstByJobIdAndCandidateId(
                        application.getJobId(),
                        application.getCandidateId()
                )
                .ifPresent(existing -> {
                    throw new DuplicateApplicationException(
                            "Candidate has already applied for this job"
                    );
                });

        if (application.getApplicationId() == null) {
            application.setApplicationId(UUID.randomUUID());
        }

        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        application.setStatus(ApplicationStatus.APPLIED);

        Application savedApplication = applicationRepository.save(application);
        String jobTitle = savedApplication.getJobTitle() != null ? savedApplication.getJobTitle() : "the selected position";
        notificationClient.sendEmail( new EmailRequest(savedApplication.getCandidateEmail(), "Application Submitted Successfully", 
            "You have successfully applied for '" + jobTitle + "'.\n\nCurrent Status: APPLIED" ));

        rabbitTemplate.convertAndSend(
                "application.exchange",
                "application.submitted",
                new ApplicationStatusChangedEvent(
                        savedApplication.getApplicationId(),
                        savedApplication.getCandidateId(),
                        savedApplication.getRecruiterId(),
                        savedApplication.getJobId(),
                        savedApplication.getStatus(),
                        savedApplication.getUpdatedAt()
                )
        );

        return savedApplication;
    }

    @Override
    public List<Application> getByCandidate(UUID candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Override
    public List<Application> getByRecruiter(UUID recruiterId) {
        return applicationRepository.findByRecruiterId(recruiterId);
    }

    @Override
    public List<Application> getByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    @Override
    public Application getById(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                "Application not found with id: " + applicationId
                        ));
    }

    /**
     * Transitions an application to a new status.
     * Sends a status-specific email notification to the candidate
     * and publishes a status-changed event to RabbitMQ.
     *
     * @param applicationId the UUID of the application to update
     * @param status        the new ApplicationStatus to set
     * @return the updated application entity
     */
    @Override
    public Application updateStatus(UUID applicationId, ApplicationStatus status) {

        Application application = getById(applicationId);

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());

        Application updatedApplication = applicationRepository.save(application);

        rabbitTemplate.convertAndSend(
                "application.exchange",
                "application.status.updated",
                new ApplicationStatusChangedEvent(
                        updatedApplication.getApplicationId(),
                        updatedApplication.getCandidateId(),
                        updatedApplication.getRecruiterId(),
                        updatedApplication.getJobId(),
                        updatedApplication.getStatus(),
                        updatedApplication.getUpdatedAt()
                )
        );

        String jobTitle = updatedApplication.getJobTitle() != null ? updatedApplication.getJobTitle() : "the selected position";
        String subject = "Application Status Updated"; 
        String body = "Your application for '" + jobTitle + "' is now marked as " + updatedApplication.getStatus(); 
        
        switch (status) { 
            case SHORTLISTED -> { 
                subject = "Application Shortlisted 🎉"; 
                body = "Congrats! Your application for '" + jobTitle + "' has been shortlisted."; 
            } 
            case INTERVIEW_SCHEDULED -> { 
                subject = "Interview Scheduled"; 
                body = "Your application for '" + jobTitle + "' has moved to the interview stage."; 
            } 
            case OFFERED -> { 
                subject = "Offer Received 🚀"; 
                body = "Congratulations! You received an offer for '" + jobTitle + "'."; 
            }
            case REJECTED -> { 
                subject = "Application Update"; 
                body = "Thank you for your interest. Unfortunately, your application for '" + jobTitle + "' was not selected this time."; 
            } 
        } 
        
        notificationClient.sendEmail( new EmailRequest(updatedApplication.getCandidateEmail(), subject, body) );

        return updatedApplication;
    }

    /**
     * Withdraws an application on behalf of the candidate.
     * Sets status to WITHDRAW, sends a confirmation email,
     * and publishes a withdrawal event to RabbitMQ.
     *
     * @param applicationId the UUID of the application to withdraw
     */
    @Override
    public void withdrawApplication(UUID applicationId) {

        Application application = getById(applicationId);

        application.setStatus(ApplicationStatus.WITHDRAW);
        application.setUpdatedAt(LocalDateTime.now());

        Application updatedApplication = applicationRepository.save(application);

        rabbitTemplate.convertAndSend(
                "application.exchange",
                "application.withdrawn",
                new ApplicationStatusChangedEvent(
                        updatedApplication.getApplicationId(),
                        updatedApplication.getCandidateId(),
                        updatedApplication.getRecruiterId(),
                        updatedApplication.getJobId(),
                        updatedApplication.getStatus(),
                        updatedApplication.getUpdatedAt()
                )
        );

        String jobTitle = updatedApplication.getJobTitle() != null ? updatedApplication.getJobTitle() : "the selected position";
        notificationClient.sendEmail( new EmailRequest(
            updatedApplication.getCandidateEmail(), 
            "Application Withdrawn", "You withdrew your application for '" + jobTitle + "'." ) );
    }

    @Override
    public long countByJob(Long jobId) {
        return applicationRepository.countByJobId(jobId);
    }

    @Override
    public long countByRecruiterId(UUID recruiterId) {
        return applicationRepository.countByRecruiterId(recruiterId);
    }

    @Override
    public long countByRecruiterIdAndStatus(UUID recruiterId, ApplicationStatus status) {
        return applicationRepository.countByRecruiterIdAndStatus(recruiterId, status);
    }

    @Override
    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }

    @Override
    public long count() {
        return applicationRepository.count();
    }

    @Override
    public Double findAverageTimeToHireByRecruiterId(UUID recruiterId) {
        Double avg = applicationRepository.findAverageTimeToHireByRecruiterId(recruiterId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public Double findPlatformAverageTimeToHire() {
        Double avg = applicationRepository.findPlatformAverageTimeToHire();
        return avg != null ? avg : 0.0;
    }

    @Override
    public List<Application> getByCandidateEmail(String email) {
        return applicationRepository.findByCandidateEmail(email);
    }

    @Override
    public long countByRecruiterIdAndStatusIn(
            UUID recruiterId,
            List<ApplicationStatus> statuses
    ) {
        return applicationRepository.countByRecruiterIdAndStatusIn(
                recruiterId,
                statuses
        );
    }

    @Override
    public long countByStatusIn(List<ApplicationStatus> statuses) {
        return applicationRepository.countByStatusIn(statuses);
    }

}