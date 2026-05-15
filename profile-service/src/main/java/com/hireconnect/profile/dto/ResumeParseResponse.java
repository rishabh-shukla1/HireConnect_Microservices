package com.hireconnect.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParseResponse {
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private List<String> experience;
}
