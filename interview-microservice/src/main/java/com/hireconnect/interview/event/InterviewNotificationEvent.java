package com.hireconnect.interview.event;

import com.hireconnect.interview.enums.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewNotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID interviewId;

    private UUID applicationId;

    private UUID candidateId;

    private UUID recruiterId;

    private LocalDateTime scheduledAt;

    private InterviewStatus status;

    private String message;
}