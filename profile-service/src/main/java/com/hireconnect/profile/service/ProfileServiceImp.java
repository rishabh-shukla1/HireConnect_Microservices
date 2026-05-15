package com.hireconnect.profile.service;

import com.hireconnect.profile.client.JobServiceClient;
import com.hireconnect.profile.dto.ProfileRequest;
import com.hireconnect.profile.dto.ProfileResponse;
import com.hireconnect.profile.dto.SavedJobResponse;
import com.hireconnect.profile.entity.CandidateProfile;
import com.hireconnect.profile.entity.RecruiterProfile;
import com.hireconnect.profile.entity.SavedJob;
import com.hireconnect.profile.exception.ProfileNotFoundException;
import com.hireconnect.profile.repository.CandidateProfileRepository;
import com.hireconnect.profile.repository.RecruiterProfileRepository;
import com.hireconnect.profile.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImp implements ProfileService {

    private final CandidateProfileRepository candidateRepository;
    private final RecruiterProfileRepository recruiterRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobServiceClient jobServiceClient;

    @Override
    public void createDefaultProfile(UUID userId, String email, String role, String mobile) {
        if ("CANDIDATE".equalsIgnoreCase(role)) {

            if (candidateRepository.existsByUserId(userId)) {
                return;
            }

            CandidateProfile profile = CandidateProfile.builder()
                    .location("")
                    .bio("")
                    .skills(List.of())
                    .experience(0)
                    .dob(LocalDate.of(2000, 1, 1))
                    .gender("Not Specified")
                    .resumeUrl("")
                    .addresses(List.of())
                    .build();

            profile.setUserId(userId);
            profile.setEmail(email);
            profile.setMobile(mobile);
            profile.setFullName("New Candidate");
            profile.setActive(true);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());

            candidateRepository.save(profile);
        }

        if ("RECRUITER".equalsIgnoreCase(role)) {

            if (recruiterRepository.existsByUserId(userId)) {
                return;
            }

            RecruiterProfile profile = RecruiterProfile.builder()
                    .companyName("")
                    .companySize("")
                    .industry("")
                    .website("")
                    .officeLocation("")
                    .build();

            profile.setUserId(userId);
            profile.setEmail(email);
            profile.setMobile(mobile);
            profile.setFullName("New Recruiter");
            profile.setActive(true);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());

            recruiterRepository.save(profile);
        }
    }

    @Override
    public ProfileResponse getCandidateProfile(UUID userId) {
        CandidateProfile profile = candidateRepository.findByUserId(userId).orElseThrow(() -> new ProfileNotFoundException(
                        "Candidate profile not found for userId: " + userId));

        return mapCandidate(profile);
    }

    @Override
    public ProfileResponse getRecruiterProfile(UUID userId) {
        RecruiterProfile profile = recruiterRepository.findByUserId(userId).orElseThrow(() -> new ProfileNotFoundException(
                        "Recruiter profile not found for userId: " + userId));

        return mapRecruiter(profile);
    }

    @Override
    public ProfileResponse updateCandidateProfile(UUID userId, ProfileRequest request) {
        CandidateProfile profile = candidateRepository.findByUserId(userId).orElseThrow(() -> new ProfileNotFoundException(
                        "Candidate profile not found for userId: " + userId));

        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setMobile(request.getMobile());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());

        profile.setSkills(
                request.getSkills() == null || request.getSkills().isBlank()
                        ? new java.util.ArrayList<>()
                        : new java.util.ArrayList<>(
                                java.util.Arrays.asList(
                                        request.getSkills().split("\\s*,\\s*"))));

        profile.setExperience(
                request.getExperience() == null || request.getExperience().isBlank()
                        ? 0
                        : Integer.parseInt(request.getExperience()));

        if (profile.getAddresses() == null) {
            profile.setAddresses(new java.util.ArrayList<>());
        }

        profile.setUpdatedAt(LocalDateTime.now());

        CandidateProfile updatedProfile = candidateRepository.save(profile);

        return mapCandidate(updatedProfile);
    }

    @Override
    public ProfileResponse updateRecruiterProfile(UUID userId, ProfileRequest request) {
        RecruiterProfile profile = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Recruiter profile not found for userId: " + userId));

        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setMobile(request.getMobile());
        profile.setCompanyName(request.getCompanyName());
        profile.setWebsite(request.getCompanyWebsite());
        profile.setIndustry(request.getHeadline());
        profile.setOfficeLocation(request.getLocation());
        profile.setUpdatedAt(LocalDateTime.now());

        return mapRecruiter(recruiterRepository.save(profile));
    }

    @Override
    public void deleteCandidateProfile(UUID userId) {
        if (!candidateRepository.existsByUserId(userId)) {
            throw new ProfileNotFoundException(
                    "Candidate profile not found for userId: " + userId);
        }

        candidateRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteRecruiterProfile(UUID userId) {
        if (!recruiterRepository.existsByUserId(userId)) {
            throw new ProfileNotFoundException(
                    "Recruiter profile not found for userId: " + userId);
        }

        recruiterRepository.deleteByUserId(userId);
    }

    @Override
    public List<ProfileResponse> getAllCandidateProfiles() {
        return candidateRepository.findAll()
                .stream()
                .map(this::mapCandidate)
                .toList();
    }

    @Override
    public List<ProfileResponse> getAllRecruiterProfiles() {
        return recruiterRepository.findAll()
                .stream()
                .map(this::mapRecruiter)
                .toList();
    }

    @Override
    public ProfileResponse getProfileByEmail(String email) {
        return candidateRepository.findByEmail(email)
                .map(this::mapCandidate)
                .orElseGet(() -> recruiterRepository.findByEmail(email)
                        .map(this::mapRecruiter)
                        .orElseGet(() -> {
                            CandidateProfile newProfile = createDefaultCandidateProfileFromEmail(email);
                            return mapCandidate(newProfile);
                        }));
    }

    @Override
    public ProfileResponse updateProfileByEmail(String email, ProfileRequest request) {
        return candidateRepository.findByEmail(email)
                .map(profile -> {
                    updateCandidateProfileFields(profile, request);
                    return mapCandidate(candidateRepository.save(profile));
                })
                .orElseGet(() -> recruiterRepository.findByEmail(email)
                        .map(profile -> {
                            updateRecruiterProfileFields(profile, request);
                            return mapRecruiter(recruiterRepository.save(profile));
                        })
                        .orElseThrow(() -> new ProfileNotFoundException(
                                "Profile not found for email: " + email)));
    }

    @Override
    public String uploadResume(String email, MultipartFile file) {
        CandidateProfile profile = candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Candidate profile not found for email: " + email));
        
        String originalName = file.getOriginalFilename() == null
                ? "resume.pdf"
                : file.getOriginalFilename();
                
        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "resumes", email);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Path filePath = uploadPath.resolve(originalName);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

        String resumeUrl = "/uploads/resumes/" + email + "/" + originalName;
        profile.setResumeUrl(resumeUrl);
        profile.setResumeName(originalName);
        profile.setUpdatedAt(LocalDateTime.now());
        candidateRepository.save(profile);

        return resumeUrl;
    }

    @Override
    public List<SavedJobResponse> getSavedJobs(String email) {
        CandidateProfile profile = candidateRepository.findByEmail(email)
                .orElseGet(() -> createDefaultCandidateProfileFromEmail(email));
        
        return savedJobRepository.findByCandidateId(profile.getUserId())
                .stream()
                .map(this::mapSavedJob)
                .toList();
    }
    
    private CandidateProfile createDefaultCandidateProfileFromEmail(String email) {
        UUID userId = UUID.nameUUIDFromBytes(email.getBytes());
        createDefaultProfile(userId, email, "CANDIDATE", "9999999999");
    
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException(
                        "Failed to create profile for email: " + email));
    }

    @Override
    public void saveJob(String email, Long jobId) {
        CandidateProfile profile = candidateRepository.findByEmail(email)
                .orElseGet(() -> createDefaultCandidateProfileFromEmail(email));
        
        if (savedJobRepository.existsByCandidateIdAndJobId(profile.getUserId(), jobId)) {
            return;
        }
        
        Map<String, Object> jobDetails = jobServiceClient.getJobById(jobId);
        
        SavedJob savedJob = SavedJob.builder()
                .candidateId(profile.getUserId())
                .jobId(jobId)
                .jobTitle(jobDetails != null ? (String) jobDetails.get("title") : "Unknown Job")
                .companyName(jobDetails != null ? (String) jobDetails.get("companyName") : "Unknown Company")
                .location(jobDetails != null ? (String) jobDetails.get("location") : "")
                .jobType(jobDetails != null ? (String) jobDetails.get("type") : "")
                .status(jobDetails != null ? (String) jobDetails.get("status") : "")
                .savedAt(LocalDateTime.now())
                .build();
        
        savedJobRepository.save(savedJob);
    }

    @Override
    public void unsaveJob(String email, Long jobId) {
        CandidateProfile profile = candidateRepository.findByEmail(email)
                .orElseGet(() -> createDefaultCandidateProfileFromEmail(email));
        
        savedJobRepository.deleteByCandidateIdAndJobId(profile.getUserId(), jobId);
    }

    private void updateCandidateProfileFields(CandidateProfile profile, ProfileRequest request) {
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setMobile(request.getMobile());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setSkills(request.getSkills() == null || request.getSkills().isBlank()
                ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>(java.util.Arrays.asList(request.getSkills().split("\\s*,\\s*"))));
        profile.setExperience(request.getExperience() == null || request.getExperience().isBlank()
                ? 0
                : Integer.parseInt(request.getExperience()));
        profile.setUpdatedAt(LocalDateTime.now());
    }

    private void updateRecruiterProfileFields(RecruiterProfile profile, ProfileRequest request) {
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setMobile(request.getMobile());
        profile.setCompanyName(request.getCompanyName());
        profile.setWebsite(request.getCompanyWebsite());
        profile.setIndustry(request.getHeadline());
        profile.setOfficeLocation(request.getLocation());
        profile.setUpdatedAt(LocalDateTime.now());
    }

    private SavedJobResponse mapSavedJob(SavedJob savedJob) {
        SavedJobResponse resp = SavedJobResponse.builder()
                .savedJobId(savedJob.getSavedJobId())
                .jobId(savedJob.getJobId())
                .jobTitle(savedJob.getJobTitle())
                .title(savedJob.getJobTitle())
                .companyName(savedJob.getCompanyName())
                .location(savedJob.getLocation())
                .type(savedJob.getJobType())
                .status(savedJob.getStatus())
                .savedAt(savedJob.getSavedAt())
                .build();


        try {
            Map<String, Object> job = jobServiceClient.getJobById(savedJob.getJobId());
            if (job != null && !job.isEmpty()) {
                if (resp.getTitle() == null) {
                    Object t = job.get("title");
                    if (t != null) {
                        resp.setTitle(t.toString());
                        resp.setJobTitle(t.toString());
                    }
                }
                if (resp.getCompanyName() == null) {
                    Object c = job.get("companyName");
                    if (c == null) c = job.get("company");
                    if (c != null) resp.setCompanyName(c.toString());
                }
                if (resp.getLocation() == null) {
                    Object l = job.get("location");
                    if (l != null) resp.setLocation(l.toString());
                }
                if (resp.getType() == null) {
                    Object t = job.get("type");
                    if (t != null) resp.setType(t.toString());
                }
                Object skills = job.get("skills");
                if (skills instanceof List<?> sList) {
                    resp.setSkills(sList.stream().map(String::valueOf).toList());
                }
                Object exp = job.get("experienceRequired");
                if (exp instanceof Number n) resp.setExperienceRequired(n.intValue());
                Object mn = job.get("minSalary");
                if (mn instanceof Number n) resp.setMinSalary(n.doubleValue());
                Object mx = job.get("maxSalary");
                if (mx instanceof Number n) resp.setMaxSalary(n.doubleValue());
            }
        } 
        catch (Exception ignored) {}
            return resp;
        }

    private ProfileResponse mapCandidate(CandidateProfile profile) {
        return ProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .mobile(profile.getMobile())
                .role("CANDIDATE")
                .location(profile.getLocation())
                .bio(profile.getBio())
                .skills(profile.getSkills() == null
                        ? ""
                        : String.join(", ", profile.getSkills()))
                .experience(profile.getExperience() == null
                        ? "0"
                        : String.valueOf(profile.getExperience()))
                .resumeUrl(profile.getResumeUrl())
                .resumeName(profile.getResumeName())
                .active(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private ProfileResponse mapRecruiter(RecruiterProfile profile) {
        return ProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .mobile(profile.getMobile())
                .role("RECRUITER")
                .companyName(profile.getCompanyName())
                .companyWebsite(profile.getWebsite())
                .headline(profile.getIndustry())
                .location(profile.getOfficeLocation())
                .active(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}