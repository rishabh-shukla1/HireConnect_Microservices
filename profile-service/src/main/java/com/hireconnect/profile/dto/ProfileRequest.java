package com.hireconnect.profile.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Mobile number must be a valid 10-digit Indian number"
    )
    private String mobile;

    private String headline;
    private String location;
    private String bio;

    // Internally stored as a comma-separated string but the UI may send either
    // a JSON array or a plain string, so the setters below coerce both shapes.
    private String skills;
    private String experience;

    private String companyName;
    private String companyWebsite;

    @JsonSetter("skills")
    public void setSkillsFlexible(Object value) {
        if (value == null) { this.skills = null; return; }
        if (value instanceof List<?> list) {
            this.skills = String.join(", ", list.stream().map(String::valueOf).toList());
        } else if (value instanceof String s) {
            this.skills = s;
        } else if (value.getClass().isArray()) {
            this.skills = String.join(", ", Arrays.stream((Object[]) value).map(String::valueOf).toList());
        } else {
            this.skills = value.toString();
        }
    }

    @JsonSetter("experience")
    public void setExperienceFlexible(Object value) {
        if (value == null) { this.experience = null; return; }
        this.experience = value.toString();
    }
}
