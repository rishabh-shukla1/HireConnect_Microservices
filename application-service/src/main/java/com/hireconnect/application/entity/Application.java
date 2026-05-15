package com.hireconnect.application.entity;

import com.hireconnect.application.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "applications",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {

    @Id
    private UUID applicationId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Column(name = "recruiter_id", nullable = false)
    private UUID recruiterId;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "resume_url")
    private String resumeUrl;

    private String jobTitle;

    @PrePersist
    public void prePersist() {
        if (applicationId == null) {
            applicationId = UUID.randomUUID();
        }

        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = ApplicationStatus.APPLIED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}