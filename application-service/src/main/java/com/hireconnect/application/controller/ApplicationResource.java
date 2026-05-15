package com.hireconnect.application.controller;

import com.hireconnect.application.client.JobServiceClient;
import com.hireconnect.application.client.ProfileServiceClient;
import com.hireconnect.application.dto.ApplicationResponse;
import com.hireconnect.application.dto.StatusUpdateRequest;
import com.hireconnect.application.entity.Application;
import com.hireconnect.application.enums.ApplicationStatus;
import com.hireconnect.application.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
public class ApplicationResource {

        private final ApplicationService applicationService;
        private final JobServiceClient jobServiceClient;
        private final ProfileServiceClient profileServiceClient;

        public ApplicationResource(ApplicationService applicationService,
                                   JobServiceClient jobServiceClient,
                                   ProfileServiceClient profileServiceClient) {
                this.applicationService = applicationService;
                this.jobServiceClient = jobServiceClient;
                this.profileServiceClient = profileServiceClient;
        }

        @PostMapping
        public ResponseEntity<ApplicationResponse> submitApplication(
                        @RequestBody Application application,
                        HttpServletRequest request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();

                String candidateIdStr = request.getHeader("X-User-Id");
                if (candidateIdStr != null && !candidateIdStr.isBlank()) {
                        application.setCandidateId(UUID.fromString(candidateIdStr));
                }
                application.setCandidateEmail(email);

                if (application.getRecruiterId() == null && application.getJobId() != null) {
                        UUID recruiterId = jobServiceClient.getPostedBy(application.getJobId());
                        if (recruiterId != null) {
                                application.setRecruiterId(recruiterId);
                        }
                }

                Application saved = applicationService.submitApplication(application);
                return ResponseEntity.ok(mapToResponse(saved, request.getHeader("Authorization")));
        }

        @GetMapping("/candidate/me")
        public ResponseEntity<List<ApplicationResponse>> getMyApplications(HttpServletRequest request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();
                String bearer = request.getHeader("Authorization");
                List<ApplicationResponse> responses = applicationService.getByCandidateEmail(email)
                                .stream()
                                .map(a -> mapToResponse(a, bearer))
                                .toList();
                return ResponseEntity.ok(responses);
        }

        @GetMapping("/recruiter/me")
        public ResponseEntity<List<ApplicationResponse>> getMyRecruiterApplications(HttpServletRequest request) {
                String recruiterIdStr = request.getHeader("X-User-Id");
                if (recruiterIdStr == null) {
                        return ResponseEntity.badRequest().build();
                }
                UUID recruiterId = UUID.fromString(recruiterIdStr);
                String bearer = request.getHeader("Authorization");
                List<ApplicationResponse> responses = applicationService.getByRecruiter(recruiterId)
                                .stream()
                                .map(a -> mapToResponse(a, bearer))
                                .toList();
                return ResponseEntity.ok(responses);
        }

        @GetMapping("/candidate/{candidateId}")
        public ResponseEntity<List<ApplicationResponse>> getByCandidate(
                        @PathVariable UUID candidateId, HttpServletRequest request) {
                String bearer = request.getHeader("Authorization");
                List<ApplicationResponse> responses = applicationService.getByCandidate(candidateId)
                                .stream()
                                .map(a -> mapToResponse(a, bearer))
                                .toList();

                return ResponseEntity.ok(responses);
        }

        @GetMapping("/job/{jobId}")
        public ResponseEntity<List<ApplicationResponse>> getByJob(
                        @PathVariable Long jobId, HttpServletRequest request) {
                String bearer = request.getHeader("Authorization");
                List<ApplicationResponse> responses = applicationService.getByJob(jobId)
                                .stream()
                                .map(a -> mapToResponse(a, bearer))
                                .toList();

                return ResponseEntity.ok(responses);
        }

        @GetMapping("/{applicationId}")
        public ResponseEntity<ApplicationResponse> getById(
                        @PathVariable UUID applicationId, HttpServletRequest request) {
                return ResponseEntity.ok(
                                mapToResponse(applicationService.getById(applicationId),
                                                request.getHeader("Authorization")));
        }

        @PutMapping("/{applicationId}/status")
        public ResponseEntity<ApplicationResponse> updateStatus(
                        @PathVariable UUID applicationId,
                        @RequestBody StatusUpdateRequest request,
                        HttpServletRequest httpRequest) {

                return ResponseEntity.ok(
                                mapToResponse(
                                                applicationService.updateStatus(applicationId, request.getStatus()),
                                                httpRequest.getHeader("Authorization")));
        }

        @PutMapping("/{applicationId}/withdraw")
        public ResponseEntity<String> withdrawApplication(@PathVariable UUID applicationId) {
                applicationService.withdrawApplication(applicationId);
                return ResponseEntity.ok("Application withdrawn successfully");
        }

        @PostMapping("/{applicationId}/shortlist")
        public ResponseEntity<ApplicationResponse> shortlistCandidate(@PathVariable UUID applicationId) {
                return ResponseEntity.ok(
                                mapToResponse(
                                                applicationService.updateStatus(
                                                                applicationId,
                                                                ApplicationStatus.SHORTLISTED)));
        }

        @PostMapping("/{applicationId}/reject")
        public ResponseEntity<ApplicationResponse> rejectCandidate(@PathVariable UUID applicationId) {
                return ResponseEntity.ok(
                                mapToResponse(
                                                applicationService.updateStatus(
                                                                applicationId,
                                                                ApplicationStatus.REJECTED)));
        }

        @PostMapping("/{applicationId}/advance")
        public ResponseEntity<ApplicationResponse> advanceCandidate(@PathVariable UUID applicationId) {
                return ResponseEntity.ok(
                                mapToResponse(
                                                applicationService.updateStatus(
                                                                applicationId,
                                                                ApplicationStatus.INTERVIEW_SCHEDULED)));
        }

        @GetMapping("/job/{jobId}/count")
        public ResponseEntity<Long> countByJob(@PathVariable Long jobId) {
                return ResponseEntity.ok(applicationService.countByJob(jobId));
        }

        @GetMapping("/recruiter/{recruiterId}/count")
        public ResponseEntity<Long> getRecruiterApplicationCount(@PathVariable UUID recruiterId) {
                return ResponseEntity.ok(applicationService.countByRecruiterId(recruiterId));
        }

       @GetMapping("/recruiter/{recruiterId}/shortlisted/count")
        public ResponseEntity<Long> getShortlistedCount(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(
                applicationService.countByRecruiterIdAndStatusIn(
                        recruiterId,
                        List.of(
                                ApplicationStatus.SHORTLISTED,
                                ApplicationStatus.INTERVIEW_SCHEDULED
                        )
                )
        );
        }

        @GetMapping("/recruiter/{recruiterId}/interview-scheduled/count")
        public ResponseEntity<Long> getInterviewScheduledCount(@PathVariable UUID recruiterId) {
        return ResponseEntity.ok(
                applicationService.countByRecruiterIdAndStatusIn(
                        recruiterId,
                        List.of(
                                ApplicationStatus.INTERVIEW_SCHEDULED)
                )
        );
        }

        @GetMapping("/recruiter/{recruiterId}/offered/count")
        public ResponseEntity<Long> getOfferedCount(@PathVariable UUID recruiterId) {
                return ResponseEntity.ok(
                                applicationService.countByRecruiterIdAndStatus(
                                                recruiterId,
                                                ApplicationStatus.OFFERED));
        }

        @GetMapping("/recruiter/{recruiterId}/rejected/count")
        public ResponseEntity<Long> getRejectedCount(@PathVariable UUID recruiterId) {
                return ResponseEntity.ok(
                                applicationService.countByRecruiterIdAndStatus(
                                                recruiterId,
                                                ApplicationStatus.REJECTED));
        }

        @GetMapping("/recruiter/{recruiterId}/avg-time-to-hire")
        public ResponseEntity<Double> getAverageTimeToHire(@PathVariable UUID recruiterId) {
                return ResponseEntity.ok(
                                applicationService.findAverageTimeToHireByRecruiterId(recruiterId));
        }

        @GetMapping("/platform/total")
        public ResponseEntity<Long> getPlatformApplicationCount() {
                return ResponseEntity.ok(applicationService.count());
        }

        @GetMapping("/platform/shortlisted/count")
        public ResponseEntity<Long> getPlatformShortlistedCount() {
                return ResponseEntity.ok(
                                applicationService.countByStatus(ApplicationStatus.SHORTLISTED));
        }

        @GetMapping("/platform/offered/count")
        public ResponseEntity<Long> getPlatformOfferedCount() {
                return ResponseEntity.ok(
                                applicationService.countByStatus(ApplicationStatus.OFFERED));
        }

        @GetMapping("/platform/rejected/count")
        public ResponseEntity<Long> getPlatformRejectedCount() {
                return ResponseEntity.ok(
                                applicationService.countByStatus(ApplicationStatus.REJECTED));
        }

        @GetMapping("/platform/avg-time-to-hire")
        public ResponseEntity<Double> getPlatformAverageTimeToHire() {
                return ResponseEntity.ok(applicationService.findPlatformAverageTimeToHire());
        }

        private ApplicationResponse mapToResponse(Application application) {
                return mapToResponse(application, null);
        }

        private ApplicationResponse mapToResponse(Application application, String bearer) {
                ApplicationResponse resp = ApplicationResponse.builder()
                                .applicationId(application.getApplicationId())
                                .jobId(application.getJobId())
                                .candidateId(application.getCandidateId())
                                .recruiterId(application.getRecruiterId())
                                .appliedAt(application.getAppliedAt())
                                .updatedAt(application.getUpdatedAt())
                                .status(application.getStatus())
                                .coverLetter(application.getCoverLetter())
                                .resumeUrl(application.getResumeUrl())
                                .candidateEmail(application.getCandidateEmail())
                                .build();

                // Enrich with job details (title, company, location, type, skills)
                if (application.getJobId() != null) {
                        var job = jobServiceClient.getJob(application.getJobId());
                        if (!job.isEmpty()) {
                                resp.setJobTitle((String) job.get("title"));
                                Object company = job.get("company");
                                if (company != null) resp.setCompanyName(company.toString());
                                resp.setLocation((String) job.get("location"));
                                Object type = job.get("type");
                                if (type != null) resp.setJobType(type.toString());
                                Object skills = job.get("skills");
                                if (skills instanceof List<?> sList) {
                                        resp.setSkills(sList.stream().map(String::valueOf).toList());
                                }
                        }
                }

                // Enrich with candidate profile (name)
                if (application.getCandidateId() != null) {
                        var candidate = profileServiceClient.getCandidate(application.getCandidateId(), bearer);
                        if (!candidate.isEmpty()) {
                                Object name = candidate.get("fullName");
                                if (name != null) resp.setCandidateName(name.toString());
                                if (resp.getCandidateEmail() == null) {
                                        Object em = candidate.get("email");
                                        if (em != null) resp.setCandidateEmail(em.toString());
                                }
                        }
                }

                return resp;
        }

       @GetMapping("/platform/interview-scheduled/count")
        public ResponseEntity<Long> getPlatformInterviewScheduledCount() {
        return ResponseEntity.ok(
                applicationService.countByStatusIn(
                        List.of(
                                ApplicationStatus.INTERVIEW_SCHEDULED
                        )
                )
        );
        }
}