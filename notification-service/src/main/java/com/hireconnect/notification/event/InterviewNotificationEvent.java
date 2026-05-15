package com.hireconnect.notification.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class InterviewNotificationEvent {

    private UUID interviewId;
    private UUID applicationId;
    private UUID candidateId;
    private UUID recruiterId;
    private LocalDateTime scheduledAt;
    private String status;
    private String message;

    public InterviewNotificationEvent() {
    }

    public UUID getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(UUID interviewId) {
        this.interviewId = interviewId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(UUID candidateId) {
        this.candidateId = candidateId;
    }

    public UUID getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(UUID recruiterId) {
        this.recruiterId = recruiterId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}