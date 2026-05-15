package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID profileId;
    private UUID userId;

    private String fullName;
    private String email;
    private String mobile;

    private String role;

    private String headline;
    private String location;
    private String bio;
    private String skills;
    private String experience;

    private String companyName;
    private String companyWebsite;

    private String resumeUrl;
    private String resumeName;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}