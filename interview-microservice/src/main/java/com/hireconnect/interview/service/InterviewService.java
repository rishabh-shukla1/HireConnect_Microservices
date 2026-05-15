package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.enums.InterviewStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InterviewService {

    Interview scheduleInterview(Interview interview);

    Interview confirmInterview(UUID interviewId);

    Interview rescheduleInterview(UUID interviewId, LocalDateTime newDateTime);

    void cancelInterview(UUID interviewId);

    List<Interview> getByCandidate(UUID candidateId);

    List<Interview> getByRecruiter(UUID recruiterId);

    List<Interview> getByApplication(UUID applicationId);

    List<Interview> getByStatus(InterviewStatus status);

    Interview getById(UUID interviewId);
    
    List<Interview> getByCandidateEmail(String email);
}