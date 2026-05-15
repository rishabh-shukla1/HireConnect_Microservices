package com.hireconnect.auth.seed;

import com.hireconnect.auth.config.JwtService;
import com.hireconnect.auth.entity.Provider;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.entity.UserRole;
import com.hireconnect.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!prod")
public class DataSeeder {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    private final String GATEWAY_URL = System.getenv().getOrDefault("GATEWAY_URL", "http://localhost:8080");

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            log.info(" Starting data seeding...");

            try {
                String forceSeed = System.getenv().getOrDefault("FORCE_SEED", "false");
                if ("true".equalsIgnoreCase(forceSeed)) {
                    log.info("Force seed enabled - clearing existing data...");
                    authRepository.deleteAll();
                }

                if (authRepository.count() == 0) {
                    seedCandidates();
                    seedRecruiters();
                    Thread.sleep(3000);
                    seedJobs();
                    log.info("Data seeding completed successfully!");
                } else {
                    log.info("Database already contains data, skipping seeding.");
                }
            } catch (Exception e) {
                log.error("Error during data seeding: {}", e.getMessage(), e);
            }
        };
    }

    private void seedCandidates() {
        log.info("Creating candidate users...");

        // These UUIDs match the profile seeders
        List<Map<String, Object>> candidates = Arrays.asList(
            createCandidateWithId("amit.patel@email.com", "Amit Patel", "7654321098", 
                UUID.fromString("802933df-bdc5-40e7-b9db-7ced2d14ede3")),
            createCandidateWithId("neha.gupta@email.com", "Neha Gupta", "6543210987",
                UUID.fromString("902933df-bdc5-40e7-b9db-7ced2d14ede4")),
            createCandidateWithId("vikram.singh@email.com", "Vikram Singh", "9532109876",
                UUID.fromString("a02933df-bdc5-40e7-b9db-7ced2d14ede5")),
            createCandidateWithId("ananya.reddy@email.com", "Ananya Reddy", "9321098765",
                UUID.fromString("b02933df-bdc5-40e7-b9db-7ced2d14ede6")),
            createCandidateWithId("rahul.verma@email.com", "Rahul Verma", "9210987654",
                UUID.fromString("c02933df-bdc5-40e7-b9db-7ced2d14ede7"))
        );

        for (Map<String, Object> candidate : candidates) {
            createUserWithId(
                (UUID) candidate.get("id"),
                (String) candidate.get("email"), 
                (String) candidate.get("name"),
                (String) candidate.get("mobile"), 
                UserRole.CANDIDATE);
        }
    }

    private Map<String, Object> createCandidateWithId(String email, String name, String mobile, UUID id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("name", name);
        map.put("mobile", mobile);
        return map;
    }

    private Map<String, String> createCandidate(String email, String name, String mobile) {
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("name", name);
        map.put("mobile", mobile);
        return map;
    }

    private void seedRecruiters() {
        log.info("Creating recruiter users with companies...");

        // These UUIDs match the profile seeders and job seeders
        List<Map<String, Object>> recruiters = Arrays.asList(
            createRecruiterWithId("rajesh.kumar@techsolutions.com", "Rajesh Kumar", "9876543210", "Tech Solutions Inc.", 
                UUID.fromString("602933df-bdc5-40e7-b9db-7ced2d14ede1")),
            createRecruiterWithId("priya.sharma@innovationlabs.com", "Priya Sharma", "8765432109", "Innovation Labs",
                UUID.fromString("702933df-bdc5-40e7-b9db-7ced2d14ede2"))
        );

        for (Map<String, Object> recruiter : recruiters) {
            UserCredential user = createUserWithId(
                (UUID) recruiter.get("id"),
                (String) recruiter.get("email"), 
                (String) recruiter.get("name"),
                (String) recruiter.get("mobile"), 
                UserRole.RECRUITER);
        }
    }

    private Map<String, Object> createRecruiterWithId(String email, String name, String mobile, String company, UUID id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("email", email);
        map.put("name", name);
        map.put("mobile", mobile);
        map.put("company", company);
        return map;
    }

    private Map<String, String> createRecruiter(String email, String name, String mobile, String company) {
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("name", name);
        map.put("mobile", mobile);
        map.put("company", company);
        return map;
    }

    private UserCredential createUserWithId(UUID id, String email, String name, String mobile, UserRole role) {
        try {
            if (authRepository.existsByEmail(email)) {
                return authRepository.findByEmail(email).orElse(null);
            }

            UserCredential user = UserCredential.builder()
                    .id(id)
                    .email(email)
                    .password(passwordEncoder.encode("password123"))
                    .role(role)
                    .provider(Provider.LOCAL)
                    .createdAt(LocalDateTime.now())
                    .lastLoginAt(LocalDateTime.now())
                    .active(true)
                    .build();

            user = authRepository.save(user);
            log.info("✓ Created user: {} ({})", email, role);
            
            String token = jwtService.generateToken(user);
            createUserProfile(token, user.getId(), name, mobile, email, role);
            
            return user;
        }
         catch (Exception e) {
            log.error("Failed to create user {}: {}", email, e.getMessage());
            return null;
        }
    }

    private void createUserProfile(String token, UUID userId, String fullName, String mobile, String email, UserRole role) {
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", userId.toString());
            profile.put("fullName", fullName);
            profile.put("email", email);
            profile.put("mobile", mobile);
            
            if (role == UserRole.CANDIDATE) {
                profile.put("headline", "Experienced " + fullName.split(" ")[1] + " looking for opportunities");
                profile.put("bio", "Passionate professional with expertise in modern technologies. Looking for challenging roles to grow my career.");
                profile.put("location", getRandomLocation());
                profile.put("skills", getRandomSkills());
                profile.put("experienceLevel", getRandomExperience());
                profile.put("preferredJobTypes", Arrays.asList("FULL_TIME", "REMOTE"));
                profile.put("expectedSalaryMin", 800000);
                profile.put("expectedSalaryMax", 2500000);
            } else {
                profile.put("headline", "Hiring Manager at " + fullName.split(" ")[0]);
                profile.put("bio", "Looking for talented professionals to join our growing team. We offer competitive packages and great work culture.");
                profile.put("location", "Bangalore, India");
                profile.put("company", fullName.split(" ")[0]);
                profile.put("position", "Senior Recruiter");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(profile, headers);
            
            restTemplate.exchange(
                GATEWAY_URL + "/profiles",
                HttpMethod.POST,
                request,
                Void.class
            );
            log.info("Created profile for: {}", fullName);
        } catch (Exception e) {
            log.warn("Could not create profile for {}: {}", fullName, e.getMessage());
        }
    }

    private void seedJobs() {
        log.info("Creating job postings...");

        List<UserCredential> recruiters = authRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.RECRUITER)
            .toList();

        List<Map<String, Object>> jobs = Arrays.asList(
            createJob("Senior Software Engineer", "Bangalore", "TECHNOLOGY",
                Arrays.asList("Java", "Spring Boot", "Microservices", "AWS"),
                5, 2500000, 4500000, "FULL_TIME"),
            
            createJob("Frontend Developer", "Hyderabad", "TECHNOLOGY",
                Arrays.asList("React", "TypeScript", "JavaScript", "CSS"),
                3, 1500000, 2800000, "FULL_TIME"),
            
            createJob("DevOps Engineer", "Bangalore", "TECHNOLOGY",
                Arrays.asList("Docker", "Kubernetes", "Azure", "Terraform"),
                4, 2000000, 3800000, "FULL_TIME"),
            
            createJob("Data Scientist", "Chennai", "TECHNOLOGY",
                Arrays.asList("Python", "Machine Learning", "SQL", "TensorFlow"),
                3, 1800000, 3500000, "FULL_TIME"),
            
            createJob("Product Manager", "Mumbai", "PRODUCT",
                Arrays.asList("Product Strategy", "Agile", "Analytics", "Leadership"),
                5, 3000000, 5500000, "FULL_TIME"),
            
            createJob("UX Designer", "Bangalore", "DESIGN",
                Arrays.asList("Figma", "UI/UX", "Prototyping", "User Research"),
                3, 1200000, 2400000, "FULL_TIME"),
            
            createJob("iOS Developer", "Hyderabad", "TECHNOLOGY",
                Arrays.asList("Swift", "iOS", "Objective-C", "Mobile Development"),
                4, 2000000, 4000000, "FULL_TIME"),
            
            createJob("Backend Engineer", "Remote", "TECHNOLOGY",
                Arrays.asList("Java", "Python", "Microservices", "Kafka"),
                4, 2200000, 4200000, "REMOTE"),
            
            createJob("Full Stack Developer", "Pune", "TECHNOLOGY",
                Arrays.asList("Node.js", "React", "MongoDB", "GraphQL"),
                3, 1800000, 3200000, "HYBRID"),
            
            createJob("Machine Learning Engineer", "Bangalore", "TECHNOLOGY",
                Arrays.asList("Python", "PyTorch", "Deep Learning", "NLP"),
                4, 2500000, 4800000, "FULL_TIME"),
            
            createJob("Cloud Architect", "Delhi", "TECHNOLOGY",
                Arrays.asList("Azure", "AWS", "GCP", "Architecture"),
                6, 3500000, 6500000, "FULL_TIME"),
            
            createJob("Android Developer", "Hyderabad", "TECHNOLOGY",
                Arrays.asList("Kotlin", "Android", "Jetpack Compose", "Firebase"),
                3, 1600000, 3000000, "FULL_TIME")
        );

        int recruiterIndex = 0;
        for (Map<String, Object> job : jobs) {
            if (recruiters.isEmpty()) break;
            
            UserCredential recruiter = recruiters.get(recruiterIndex % recruiters.size());
            String token = jwtService.generateToken(recruiter);
            
            createJobPosting(token, job);
            recruiterIndex++;
        }
    }

    private Map<String, Object> createJob(String title, String location, String category,
            List<String> skills, int expYears, double minSalary, double maxSalary, String type) {
        Map<String, Object> job = new HashMap<>();
        job.put("title", title);
        job.put("location", location);
        job.put("category", category);
        job.put("skillsRequired", skills);
        job.put("experienceRequired", expYears);
        job.put("minSalary", minSalary);
        job.put("maxSalary", maxSalary);
        job.put("type", type);
        job.put("description", buildJobDescription(title, expYears, skills));
        return job;
    }

    private String buildJobDescription(String title, int expYears, List<String> skills) {
        return "We are looking for a talented " + title + " to join our growing team.\n\n" +
            "Responsibilities:\n" +
            "• Design, develop, and maintain high-quality software solutions\n" +
            "• Collaborate with cross-functional teams to define and implement new features\n" +
            "• Write clean, scalable, and well-documented code\n" +
            "• Participate in code reviews and mentor junior developers\n\n" +
            "Requirements:\n" +
            "• " + expYears + "+ years of professional experience\n" +
            "• Strong proficiency in " + String.join(", ", skills.subList(0, Math.min(3, skills.size()))) + "\n" +
            "• Bachelor's degree in Computer Science or equivalent\n" +
            "• Excellent problem-solving and communication skills\n\n" +
            "What We Offer:\n" +
            "• Competitive salary package\n" +
            "• Comprehensive health insurance\n" +
            "• Flexible working hours and remote options\n" +
            "• Professional development and learning opportunities\n" +
            "• Modern tech stack and collaborative work environment";
    }

    private void createJobPosting(String token, Map<String, Object> job) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(job, headers);
            
            restTemplate.exchange(
                GATEWAY_URL + "/jobs",
                HttpMethod.POST,
                request,
                Void.class
            );
            log.info("✓ Created job: {}", job.get("title"));
        } catch (Exception e) {
            log.warn("Could not create job {}: {}", job.get("title"), e.getMessage());
        }
    }

    private String getRandomLocation() {
        List<String> locations = Arrays.asList(
            "Bangalore, India", "Hyderabad, India", "Chennai, India", 
            "Mumbai, India", "Pune, India", "Delhi, India", "Remote"
        );
        return locations.get(new Random().nextInt(locations.size()));
    }

    private List<String> getRandomSkills() {
        List<String> allSkills = Arrays.asList(
            "Java", "Python", "JavaScript", "React", "Angular", "Vue.js",
            "Node.js", "Spring Boot", "Docker", "Kubernetes", "AWS", "Azure",
            "GCP", "SQL", "MongoDB", "PostgreSQL", "Redis", "Kafka",
            "Microservices", "REST APIs", "GraphQL", "TypeScript", "Git"
        );
        
        Collections.shuffle(allSkills);
        return allSkills.subList(0, Math.min(5, allSkills.size()));
    }

    private String getRandomExperience() {
        List<String> levels = Arrays.asList("ENTRY_LEVEL", "MID_LEVEL", "SENIOR_LEVEL");
        return levels.get(new Random().nextInt(levels.size()));
    }
}