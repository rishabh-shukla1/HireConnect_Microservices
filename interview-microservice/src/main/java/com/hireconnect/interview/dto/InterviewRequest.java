package com.hireconnect.interview.dto;

import com.hireconnect.interview.enums.InterviewMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Recruiter ID is required")
    private UUID recruiterId;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Interview time must be in the future")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Interview mode is required")
    private InterviewMode mode;

    private String meetLink;

    private String location;

    @NotBlank(message = "Interview notes are required")
    private String notes;
}