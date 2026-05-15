package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByCategoryIgnoreCase(String category);

    List<Job> findByLocationIgnoreCase(String location);

    List<Job> findByPostedBy(UUID postedBy);

    List<Job> findByPostedByAndStatus(UUID postedBy, JobStatus status);

    List<Job> findByStatus(JobStatus status);

    long countByPostedBy(UUID postedBy);

    long countByCategoryIgnoreCase(String category);

    Long countByPostedByAndStatus(UUID postedBy, JobStatus status);
}