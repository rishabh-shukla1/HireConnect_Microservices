package com.hireconnect.interview.controller;

import com.hireconnect.interview.dto.InterviewRequest;
import com.hireconnect.interview.dto.InterviewResponse;
import com.hireconnect.interview.dto.RescheduleRequest;
import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.enums.InterviewStatus;
import com.hireconnect.interview.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/interviews")
public class InterviewResource {

    private final InterviewService interviewService;

    public InterviewResource(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> scheduleInterview(
            @Valid @RequestBody InterviewRequest request) {

        Interview interview = new Interview();
        interview.setApplicationId(request.getApplicationId());
        interview.setCandidateId(request.getCandidateId());
        interview.setRecruiterId(request.getRecruiterId());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setMode(request.getMode());
        interview.setMeetLink(request.getMeetLink());
        interview.setLocation(request.getLocation());
        interview.setNotes(request.getNotes());

        Interview saved = interviewService.scheduleInterview(interview);

        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<InterviewResponse> confirm(@PathVariable UUID id) {

        Interview interview = interviewService.confirmInterview(id);

        return ResponseEntity.ok(mapToResponse(interview));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<InterviewResponse> reschedule(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleRequest request) {

        Interview interview = interviewService.rescheduleInterview(
                id,
                request.getNewDateTime()
        );

        return ResponseEntity.ok(mapToResponse(interview));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable UUID id) {

        interviewService.cancelInterview(id);

        return ResponseEntity.ok("Interview cancelled successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                mapToResponse(interviewService.getById(id))
        );
    }

    @GetMapping("/my-interviews")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(
            jakarta.servlet.http.HttpServletRequest request) {
        // Prefer candidateId propagated from the gateway (X-User-Id). Fall back
        // to looking up by email from the SecurityContext in case an older
        // gateway doesn't set the header.
        List<Interview> interviews = null;
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                UUID candidateId = UUID.fromString(userIdHeader);
                interviews = interviewService.getByCandidate(candidateId);
            } catch (IllegalArgumentException ignored) { /* bad UUID; try email */ }
        }
        if (interviews == null || interviews.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication != null ? authentication.getName() : null;
            if (email != null) {
                interviews = interviewService.getByCandidateEmail(email);
            }
        }
        if (interviews == null) interviews = List.of();
        return ResponseEntity.ok(
                interviews.stream().map(this::mapToResponse).toList()
        );
    }

    @GetMapping("/candidate/me")
    public ResponseEntity<List<InterviewResponse>> getCandidateMyInterviews(
            jakarta.servlet.http.HttpServletRequest request
    ) {
        return getMyInterviews(request);
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<InterviewResponse>> getByCandidate(
            @PathVariable UUID candidateId) {

        return ResponseEntity.ok(
                interviewService.getByCandidate(candidateId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<InterviewResponse>> getByRecruiter(
            @PathVariable UUID recruiterId) {

        return ResponseEntity.ok(
                interviewService.getByRecruiter(recruiterId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<InterviewResponse>> getByApplication(
            @PathVariable UUID applicationId) {

        return ResponseEntity.ok(
                interviewService.getByApplication(applicationId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InterviewResponse>> getByStatus(
            @PathVariable InterviewStatus status) {

        return ResponseEntity.ok(
                interviewService.getByStatus(status)
                        .stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    private InterviewResponse mapToResponse(Interview interview) {
        return new InterviewResponse(
                interview.getInterviewId(),
                interview.getApplicationId(),
                interview.getCandidateId(),
                interview.getRecruiterId(),
                interview.getScheduledAt(),
                interview.getMode(),
                interview.getMeetLink(),
                interview.getLocation(),
                interview.getStatus(),
                interview.getNotes(),
                interview.getCreatedAt(),
                interview.getUpdatedAt()
        );
    }
}