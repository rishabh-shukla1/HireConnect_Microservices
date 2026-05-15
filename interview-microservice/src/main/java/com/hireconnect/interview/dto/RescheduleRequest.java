package com.hireconnect.interview.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RescheduleRequest {

    @NotNull(message = "New interview time is required")
    @Future(message = "Rescheduled interview time must be in the future")
    private LocalDateTime newDateTime;
}