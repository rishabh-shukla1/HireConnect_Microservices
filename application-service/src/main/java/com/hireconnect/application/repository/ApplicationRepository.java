package com.hireconnect.application.repository;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByCandidateId(UUID candidateId);

    List<Application> findByCandidateEmail(String candidateEmail);

    List<Application> findByJobId(Long jobId);

    List<Application> findByRecruiterId(UUID recruiterId);

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByAppliedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByJobId(Long jobId);

    Optional<Application> findFirstByJobIdAndCandidateId(Long jobId, UUID candidateId);

    long countByRecruiterId(UUID recruiterId);

    long countByRecruiterIdAndStatus(UUID recruiterId, ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    @Query(value = """
            SELECT AVG(TIMESTAMPDIFF(DAY, applied_at, updated_at))
            FROM applications
            WHERE recruiter_id = :recruiterId
              AND status = 'OFFERED'
            """, nativeQuery = true)
    Double findAverageTimeToHireByRecruiterId(UUID recruiterId);

    @Query(value = """
            SELECT AVG(TIMESTAMPDIFF(DAY, applied_at, updated_at))
            FROM applications
            WHERE status = 'OFFERED'
            """, nativeQuery = true)
    Double findPlatformAverageTimeToHire();

    long countByRecruiterIdAndStatusIn(UUID recruiterId, List<ApplicationStatus> statuses);

    long countByStatusIn(List<ApplicationStatus> statuses);

}