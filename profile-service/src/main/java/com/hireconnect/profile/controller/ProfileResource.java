package com.hireconnect.profile.controller;

import com.hireconnect.profile.dto.ProfileRequest;
import com.hireconnect.profile.dto.ProfileResponse;
import com.hireconnect.profile.dto.ResumeParseResponse;
import com.hireconnect.profile.dto.SavedJobResponse;
import com.hireconnect.profile.service.ProfileService;
import com.hireconnect.profile.util.ResumeParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileResource {

    private final ProfileService profileService;
    private final ResumeParser resumeParser;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.getProfileByEmail(email));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody ProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.updateProfileByEmail(email, request));
    }

    @PostMapping("/resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String resumeUrl = profileService.uploadResume(email, file);
        return ResponseEntity.ok(resumeUrl);
    }

    @PostMapping("/resume/parse")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ResumeParseResponse> parseResume(
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        MultipartFile incoming = resume != null ? resume : file;
        if (incoming == null || incoming.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resumeParser.parse(incoming));
    }

    @GetMapping("/saved-jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<SavedJobResponse>> getSavedJobs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.getSavedJobs(email));
    }

    @PostMapping("/saved-jobs/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> saveJob(@PathVariable Long jobId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        profileService.saveJob(email, jobId);
        return ResponseEntity.ok("Job saved successfully");
    }

    @DeleteMapping("/saved-jobs/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> unsaveJob(@PathVariable Long jobId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        profileService.unsaveJob(email, jobId);
        return ResponseEntity.ok("Job removed from saved list");
    }

    @GetMapping("/candidates/{userId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    public ResponseEntity<ProfileResponse> getCandidateProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getCandidateProfile(userId));
    }

    @GetMapping("/candidates")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<ProfileResponse>> getAllCandidateProfiles() {
        return ResponseEntity.ok(profileService.getAllCandidateProfiles());
    }

    @PutMapping("/candidates/{userId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ProfileResponse> updateCandidateProfile( @PathVariable UUID userId, @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(
                profileService.updateCandidateProfile(userId, request)
        );
    }

    @DeleteMapping("/candidates/{userId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> deleteCandidateProfile(@PathVariable UUID userId) {
        profileService.deleteCandidateProfile(userId);

        return ResponseEntity.ok("Candidate profile deleted successfully");
    }

    @GetMapping("/recruiters/{userId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'CANDIDATE')")
    public ResponseEntity<ProfileResponse> getRecruiterProfile(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(profileService.getRecruiterProfile(userId));
    }

    @GetMapping("/recruiters")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<ProfileResponse>> getAllRecruiterProfiles() {
        return ResponseEntity.ok(profileService.getAllRecruiterProfiles());
    }

    @PutMapping("/recruiters/{userId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ProfileResponse> updateRecruiterProfile(@PathVariable UUID userId, @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(
                profileService.updateRecruiterProfile(userId, request)
        );
    }

    @DeleteMapping("/recruiters/{userId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<String> deleteRecruiterProfile(@PathVariable UUID userId) {
        profileService.deleteRecruiterProfile(userId);
        return ResponseEntity.ok("Recruiter profile deleted successfully");
    }
}