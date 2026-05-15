package com.hireconnect.application.event;

import com.hireconnect.application.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusChangedEvent {

    private UUID applicationId;
    private UUID candidateId;
    private UUID recruiterId;
    private Long jobId;

    private ApplicationStatus status;

    private LocalDateTime updatedAt;
}