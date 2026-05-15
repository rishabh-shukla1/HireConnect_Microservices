package com.hireconnect.job.entity;

import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.enums.JobType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobType type;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false)
    private Double minSalary;

    @Column(nullable = false)
    private Double maxSalary;

    @Column(nullable = false, length = 5000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill", nullable = false, length = 50)
    private List<String> skills;

    @Column(nullable = false)
    private Integer experienceRequired;

    @Column(nullable = false)
    private UUID postedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime postedAt;

    @PrePersist
    public void prePersist() {
        if (postedAt == null) {
            postedAt = LocalDateTime.now();
        }

        if (status == null) {
            status = JobStatus.OPEN;
        }

        if (viewCount == null) {
            viewCount = 0L;
        }
    }

    @Column
    private String company;

    @Column(nullable = false)
    private Long viewCount = 0L;
}