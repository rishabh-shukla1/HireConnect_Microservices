package com.hireconnect.profile.service;

import com.hireconnect.profile.dto.ProfileRequest;
import com.hireconnect.profile.dto.ProfileResponse;
import com.hireconnect.profile.dto.SavedJobResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProfileService {

    void createDefaultProfile(
            UUID userId,
            String email,
            String role,
            String mobile
    );

    ProfileResponse getCandidateProfile(UUID userId);

    ProfileResponse getRecruiterProfile(UUID userId);

    ProfileResponse updateCandidateProfile(UUID userId, ProfileRequest request);

    ProfileResponse updateRecruiterProfile(UUID userId, ProfileRequest request);

    void deleteCandidateProfile(UUID userId);

    void deleteRecruiterProfile(UUID userId);

    List<ProfileResponse> getAllCandidateProfiles();

    List<ProfileResponse> getAllRecruiterProfiles();
    
    ProfileResponse getProfileByEmail(String email);
    
    ProfileResponse updateProfileByEmail(String email, ProfileRequest request);
    
    String uploadResume(String email, MultipartFile file);
    
    List<SavedJobResponse> getSavedJobs(String email);
    
    void saveJob(String email, Long jobId);
    
    void unsaveJob(String email, Long jobId);
}