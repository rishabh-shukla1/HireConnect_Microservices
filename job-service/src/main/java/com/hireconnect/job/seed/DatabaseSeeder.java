package com.hireconnect.job.seed;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.enums.JobType;
import com.hireconnect.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final JobRepository jobRepository;

    @Override
    public void run(String... args) {
        if (jobRepository.count() == 0) {
            log.info("Seeding database with sample jobs...");
            seedJobs();
            log.info("Database seeding completed!");
        } else {
            log.info("Database already contains jobs, skipping seeding.");
        }
    }

    private void seedJobs() {
        UUID recruiter1 = UUID.fromString("602933df-bdc5-40e7-b9db-7ced2d14ede1");
        UUID recruiter2 = UUID.fromString("702933df-bdc5-40e7-b9db-7ced2d14ede2");

        List<Job> jobs = List.of(

                Job.builder()
                        .title("Senior Java Developer")
                        .company("Tech Solutions Inc.")
                        .category("Software Development")
                        .description("We are looking for an experienced Java developer to join our team.")
                        .skills(List.of("Java", "Spring Boot", "Hibernate", "SQL", "Microservices", "Docker",
                                "Kubernetes"))
                        .location("Bangalore, India")
                        .minSalary(1500000.0)
                        .maxSalary(2500000.0)
                        .type(JobType.FULL_TIME)
                        .experienceRequired(5)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter1)
                        .postedAt(LocalDateTime.now().minusDays(7))
                        .build(),

                Job.builder()
                        .title("React Frontend Developer")
                        .company("Innovation Labs")
                        .category("Frontend Development")
                        .description("Join our dynamic frontend team building modern web applications.")
                        .skills(List.of("React", "JavaScript", "TypeScript", "HTML5", "CSS3", "Redux", "Git"))
                        .location("Hyderabad, India")
                        .minSalary(800000.0)
                        .maxSalary(1200000.0)
                        .type(JobType.FULL_TIME)
                        .experienceRequired(2)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter1)
                        .postedAt(LocalDateTime.now().minusDays(12))
                        .build(),

                Job.builder()
                        .title("Full Stack Developer")
                        .company("Digital Transformers")
                        .category("Full Stack Development")
                        .description(
                                "Looking for a versatile full stack developer comfortable with both frontend and backend technologies.")
                        .skills(List.of("React", "Node.js", "Python", "PostgreSQL", "MongoDB", "AWS", "Docker"))
                        .location("Remote")
                        .minSalary(1200000.0)
                        .maxSalary(2000000.0)
                        .type(JobType.REMOTE)
                        .experienceRequired(3)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter2)
                        .postedAt(LocalDateTime.now().minusDays(3))
                        .build(),

                Job.builder()
                        .title("DevOps Engineer")
                        .company("Cloud Systems Pvt Ltd")
                        .category("DevOps")
                        .description(
                                "Join our infrastructure team to build and maintain scalable cloud infrastructure.")
                        .skills(List.of("AWS", "Azure", "Kubernetes", "Docker", "Terraform", "Jenkins", "Prometheus"))
                        .location("Pune, India")
                        .minSalary(1800000.0)
                        .maxSalary(3000000.0)
                        .type(JobType.FULL_TIME)
                        .experienceRequired(5)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter1)
                        .postedAt(LocalDateTime.now().minusDays(5))
                        .build(),

                Job.builder()
                        .title("Data Scientist")
                        .company("AI Innovations")
                        .category("Data Science")
                        .description("Help us build intelligent systems using machine learning and AI.")
                        .skills(List.of("Python", "TensorFlow", "PyTorch", "Machine Learning", "Deep Learning", "SQL",
                                "Statistics"))
                        .location("Bangalore, India")
                        .minSalary(2000000.0)
                        .maxSalary(3500000.0)
                        .type(JobType.FULL_TIME)
                        .experienceRequired(4)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter2)
                        .postedAt(LocalDateTime.now().minusDays(1))
                        .build(),

                Job.builder()
                        .title("Mobile App Developer (Flutter)")
                        .company("MobileFirst Solutions")
                        .category("Mobile Development")
                        .description("Build beautiful cross-platform mobile applications using Flutter.")
                        .skills(List.of("Flutter", "Dart", "Firebase", "REST APIs", "Git", "Mobile UI/UX"))
                        .location("Chennai, India")
                        .minSalary(600000.0)
                        .maxSalary(1200000.0)
                        .type(JobType.FULL_TIME)
                        .experienceRequired(1)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter1)
                        .postedAt(LocalDateTime.now().minusDays(8))
                        .build(),

                Job.builder()
                        .title("UX/UI Designer")
                        .company("Creative Studio")
                        .category("Design")
                        .description(
                                "Design intuitive and visually appealing interfaces for web and mobile applications.")
                        .skills(List.of("Figma", "Adobe XD", "UI Design", "UX Research", "Prototyping",
                                "Design Systems"))
                        .location("Mumbai, India")
                        .minSalary(800000.0)
                        .maxSalary(1600000.0)
                        .type(JobType.HYBRID)
                        .experienceRequired(3)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter2)
                        .postedAt(LocalDateTime.now().minusDays(4))
                        .build(),

                Job.builder()
                        .title("Backend Engineer (Node.js)")
                        .company("StartupXYZ")
                        .category("Backend Development")
                        .description("Join our fast-growing startup as a backend engineer.")
                        .skills(List.of("Node.js", "Express", "MongoDB", "Redis", "REST APIs", "Microservices",
                                "Docker"))
                        .location("Delhi, India")
                        .minSalary(1000000.0)
                        .maxSalary(1800000.0)
                        .type(JobType.FULL_TIME)
                        .experienceRequired(3)
                        .status(JobStatus.OPEN)
                        .postedBy(recruiter1)
                        .postedAt(LocalDateTime.now().minusDays(6))
                        .build());

        jobRepository.saveAll(jobs);
        log.info("Seeded {} jobs", jobs.size());
    }
}
