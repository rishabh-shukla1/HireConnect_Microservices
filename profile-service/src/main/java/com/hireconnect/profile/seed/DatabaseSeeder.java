package com.hireconnect.profile.seed;

import com.hireconnect.profile.entity.CandidateProfile;
import com.hireconnect.profile.entity.RecruiterProfile;
import com.hireconnect.profile.repository.CandidateProfileRepository;
import com.hireconnect.profile.repository.RecruiterProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final CandidateProfileRepository candidateRepository;
    private final RecruiterProfileRepository recruiterRepository;

    @Override
    public void run(String... args) {
        if (candidateRepository.count() == 0 && recruiterRepository.count() == 0) {
            log.info("Seeding database with sample profiles...");
            seedProfiles();
            log.info("Profile seeding completed!");
        } else {
            log.info("Database already contains profiles, skipping seeding.");
        }
    }

    private void seedProfiles() {
        UUID recruiter1 = UUID.fromString("602933df-bdc5-40e7-b9db-7ced2d14ede1");
        UUID recruiter2 = UUID.fromString("702933df-bdc5-40e7-b9db-7ced2d14ede2");

        RecruiterProfile recruiterProfile1 = RecruiterProfile.builder()
                .userId(recruiter1)
                .fullName("Rajesh Kumar")
                .email("rajesh.kumar@techsolutions.com")
                .mobile("9876543210")
                .companyName("Tech Solutions Inc.")
                .industry("Information Technology")
                .companySize("100-500 employees")
                .officeLocation("Bangalore, Karnataka")
                .website("https://techsolutions.com")
                .build();

        RecruiterProfile recruiterProfile2 = RecruiterProfile.builder()
                .userId(recruiter2)
                .fullName("Priya Sharma")
                .email("priya.sharma@innovationlabs.com")
                .mobile("8765432109")
                .companyName("Innovation Labs")
                .industry("Software Development")
                .companySize("50-200 employees")
                .officeLocation("Hyderabad, Telangana")
                .website("https://innovationlabs.io")
                .build();

        recruiterRepository.save(recruiterProfile1);
        recruiterRepository.save(recruiterProfile2);

        // Seed Candidate Profiles
        UUID[] candidateIds = {
            UUID.fromString("802933df-bdc5-40e7-b9db-7ced2d14ede3"),
            UUID.fromString("902933df-bdc5-40e7-b9db-7ced2d14ede4"),
            UUID.fromString("a02933df-bdc5-40e7-b9db-7ced2d14ede5"),
            UUID.fromString("b02933df-bdc5-40e7-b9db-7ced2d14ede6"),
            UUID.fromString("c02933df-bdc5-40e7-b9db-7ced2d14ede7")
        };

        String[][] candidateData = {
            {"Amit Patel", "amit.patel@email.com", "7654321098", "Bangalore", "Software Engineer", "5"},
            {"Neha Gupta", "neha.gupta@email.com", "6543210987", "Mumbai", "Frontend Developer", "3"},
            {"Vikram Singh", "vikram.singh@email.com", "6543210976", "Delhi", "Full Stack Developer", "4"},
            {"Ananya Reddy", "ananya.reddy@email.com", "7321098765", "Hyderabad", "Data Scientist", "2"},
            {"Rahul Verma", "rahul.verma@email.com", "8210987654", "Pune", "DevOps Engineer", "6"}
        };

        String[] skills = {
            "Java, Spring Boot, Hibernate, MySQL, Docker, AWS",
            "React, JavaScript, TypeScript, HTML, CSS, Redux",
            "Python, Django, React, PostgreSQL, Redis, Docker",
            "Python, Machine Learning, TensorFlow, SQL, Statistics",
            "AWS, Kubernetes, Docker, Jenkins, Terraform, Linux"
        };

        String[] summaries = {
            "Passionate software engineer with expertise in building scalable Java applications. Experienced in microservices architecture and cloud deployment.",
            "Creative frontend developer focused on building responsive and user-friendly web applications. Strong eye for design and attention to detail.",
            "Versatile full stack developer with experience in both frontend and backend technologies. Love solving complex problems and learning new technologies.",
            "Data enthusiast with strong analytical skills. Experience in building predictive models and extracting insights from complex datasets.",
            "DevOps engineer with deep knowledge of cloud infrastructure and automation. Passionate about CI/CD and infrastructure as code."
        };

        for (int i = 0; i < candidateIds.length; i++) {
            CandidateProfile candidate = CandidateProfile.builder()
                    .userId(candidateIds[i])
                    .fullName(candidateData[i][0])
                    .email(candidateData[i][1])
                    .mobile(candidateData[i][2])
                    .location(candidateData[i][3])
                    .experience(Integer.parseInt(candidateData[i][5]))
                    .skills(List.of(skills[i].split(", ")))
                    .bio(summaries[i])
                    .resumeUrl("https://storage.hireconnect.com/resumes/" + candidateIds[i] + ".pdf")
                    .build();
            
            candidateRepository.save(candidate);
        }

        log.info("Seeded 2 recruiter profiles and {} candidate profiles", candidateIds.length);
    }
}
