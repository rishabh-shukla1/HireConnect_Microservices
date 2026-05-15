package com.hireconnect.application.service;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.enums.ApplicationStatus;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    Application submitApplication(Application application);

    List<Application> getByCandidate(UUID candidateId);

    List<Application> getByRecruiter(UUID recruiterId);

    List<Application> getByJob(Long jobId);

    Application getById(UUID applicationId);

    Application updateStatus(UUID applicationId, ApplicationStatus status);

    void withdrawApplication(UUID applicationId);

    long countByJob(Long jobId);

    long countByRecruiterId(UUID recruiterId);

    long countByRecruiterIdAndStatus(UUID recruiterId, ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    long count();

    Double findAverageTimeToHireByRecruiterId(UUID recruiterId);

    Double findPlatformAverageTimeToHire();
    
    List<Application> getByCandidateEmail(String email);

    long countByRecruiterIdAndStatusIn(UUID recruiterId, List<ApplicationStatus> statuses);

    long countByStatusIn(List<ApplicationStatus> statuses);
}