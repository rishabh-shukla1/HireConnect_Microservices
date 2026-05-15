package com.hireconnect.interview.repository;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    List<Interview> findByCandidateId(UUID candidateId);

    List<Interview> findByCandidateEmail(String candidateEmail);

    List<Interview> findByRecruiterId(UUID recruiterId);

    List<Interview> findByApplicationId(UUID applicationId);

    List<Interview> findByStatus(InterviewStatus status);

    List<Interview> findByScheduledAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Interview> findByInterviewId(UUID interviewId);

    boolean existsByApplicationId(UUID applicationId);

    boolean existsByScheduledAtAndRecruiterIdAndStatusNot(
            LocalDateTime scheduledAt,
            UUID recruiterId,
            InterviewStatus status
    );
}