package com.hireconnect.application.seed;

import com.hireconnect.application.entity.Application;
import com.hireconnect.application.enums.ApplicationStatus;
import com.hireconnect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;

    @Override
    public void run(String... args) {
        if (applicationRepository.count() == 0) {
            log.info("Seeding database with sample applications...");
            seedApplications();
            log.info("Application seeding completed!");
        } else {
            log.info("Database already contains applications, skipping seeding.");
        }
    }

    private void seedApplications() {
        UUID recruiter1 = UUID.fromString("602933df-bdc5-40e7-b9db-7ced2d14ede1");
        UUID recruiter2 = UUID.fromString("702933df-bdc5-40e7-b9db-7ced2d14ede2");

        // Candidate IDs
        UUID[] candidates = {
            UUID.fromString("802933df-bdc5-40e7-b9db-7ced2d14ede3"),
            UUID.fromString("902933df-bdc5-40e7-b9db-7ced2d14ede4"),
            UUID.fromString("a02933df-bdc5-40e7-b9db-7ced2d14ede5"),
            UUID.fromString("b02933df-bdc5-40e7-b9db-7ced2d14ede6"),
            UUID.fromString("c02933df-bdc5-40e7-b9db-7ced2d14ede7")
        };

        List<Application> applications = List.of(
            // Applications for Java Developer job (jobId = 1)
            Application.builder()
                    .jobId(1L)
                    .candidateId(candidates[0])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.APPLIED)
                    .coverLetter("I am excited to apply for the Senior Java Developer position. With 5 years of experience in Spring Boot and microservices, I believe I would be a great fit for your team.")
                    .appliedAt(LocalDateTime.now().minusDays(2))
                    .updatedAt(LocalDateTime.now().minusDays(2))
                    .build(),

            Application.builder()
                    .jobId(1L)
                    .candidateId(candidates[2])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.SHORTLISTED)
                    .coverLetter("As a Full Stack Developer with strong Java backend skills, I'm particularly interested in your microservices architecture. I have experience with Docker and Kubernetes as well.")
                    .appliedAt(LocalDateTime.now().minusDays(5))
                    .updatedAt(LocalDateTime.now().minusDays(3))
                    .build(),

            // Applications for React Developer job (jobId = 2)
            Application.builder()
                    .jobId(2L)
                    .candidateId(candidates[1])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.INTERVIEW_SCHEDULED)
                    .coverLetter("I have 3 years of experience building React applications with Redux and TypeScript. I'm passionate about creating responsive and accessible user interfaces.")
                    .appliedAt(LocalDateTime.now().minusDays(3))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build(),

            Application.builder()
                    .jobId(2L)
                    .candidateId(candidates[3])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.APPLIED)
                    .coverLetter("Although my background is in Data Science, I've been learning React and building side projects. I'm eager to transition into frontend development.")
                    .appliedAt(LocalDateTime.now().minusDays(1))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build(),

            // Applications for Full Stack job (jobId = 3)
            Application.builder()
                    .jobId(3L)
                    .candidateId(candidates[2])
                    .recruiterId(recruiter2)
                    .status(ApplicationStatus.OFFERED)
                    .coverLetter("With my experience in both React and Node.js, I believe I'm perfect for this Full Stack role. I've worked on several projects using the exact tech stack you mentioned.")
                    .appliedAt(LocalDateTime.now().minusDays(7))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build(),

            Application.builder()
                    .jobId(3L)
                    .candidateId(candidates[0])
                    .recruiterId(recruiter2)
                    .status(ApplicationStatus.REJECTED)
                    .coverLetter("I have extensive Java experience and have recently started learning React. I'm confident I can quickly get up to speed with your tech stack.")
                    .appliedAt(LocalDateTime.now().minusDays(4))
                    .updatedAt(LocalDateTime.now().minusDays(2))
                    .build(),

            // Applications for DevOps job (jobId = 4)
            Application.builder()
                    .jobId(4L)
                    .candidateId(candidates[4])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.SHORTLISTED)
                    .coverLetter("As a DevOps Engineer with 6 years of experience, I've managed large-scale Kubernetes clusters and implemented robust CI/CD pipelines. I'm excited about the challenges at Cloud Systems.")
                    .appliedAt(LocalDateTime.now().minusDays(3))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build(),

            // Applications for Data Scientist job (jobId = 5)
            Application.builder()
                    .jobId(5L)
                    .candidateId(candidates[3])
                    .recruiterId(recruiter2)
                    .status(ApplicationStatus.INTERVIEW_SCHEDULED)
                    .coverLetter("I hold a Master's degree in Data Science and have published 2 papers on NLP. I'm passionate about applying ML to solve real-world business problems.")
                    .appliedAt(LocalDateTime.now().minusDays(2))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build(),

            // More applications for variety
            Application.builder()
                    .jobId(6L)
                    .candidateId(candidates[1])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.APPLIED)
                    .coverLetter("I have 2 years of experience with Flutter and have published 3 apps to the Play Store. I'm excited about the opportunity to work on mobile applications.")
                    .appliedAt(LocalDateTime.now().minusHours(12))
                    .updatedAt(LocalDateTime.now().minusHours(12))
                    .build(),

            Application.builder()
                    .jobId(8L)
                    .candidateId(candidates[0])
                    .recruiterId(recruiter1)
                    .status(ApplicationStatus.SHORTLISTED)
                    .coverLetter("While my primary experience is in Java, I've been working with Node.js for the past year and have built several RESTful APIs using Express and MongoDB.")
                    .appliedAt(LocalDateTime.now().minusDays(6))
                    .updatedAt(LocalDateTime.now().minusDays(4))
                    .build()
        );

        applicationRepository.saveAll(applications);
        log.info("Seeded {} applications", applications.size());
    }
}
