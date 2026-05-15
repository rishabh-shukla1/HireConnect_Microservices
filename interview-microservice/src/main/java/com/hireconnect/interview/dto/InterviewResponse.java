package com.hireconnect.interview.dto;

import com.hireconnect.interview.enums.InterviewMode;
import com.hireconnect.interview.enums.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewResponse {

    private UUID interviewId;

    private UUID applicationId;

    private UUID candidateId;

    private UUID recruiterId;

    private LocalDateTime scheduledAt;

    private InterviewMode mode;

    private String meetLink;

    private String location;

    private InterviewStatus status;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}