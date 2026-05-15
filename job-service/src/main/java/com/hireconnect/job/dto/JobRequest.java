package com.hireconnect.job.dto;

import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.enums.JobType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Job category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @NotNull(message = "Job type is required")
    private JobType type;

    @NotBlank(message = "Job location is required")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @NotNull(message = "Minimum salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum salary must be greater than 0")
    private Double minSalary;

    @NotNull(message = "Maximum salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum salary must be greater than 0")
    private Double maxSalary;

    @NotNull(message = "Skills are required")
    @Size(min = 1, message = "At least one skill is required")
    private List<
            @NotBlank(message = "Skill cannot be blank")
            @Size(max = 50, message = "Skill cannot exceed 50 characters")
            String> skills;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceRequired;

    private JobStatus status;

    @NotNull(message = "Recruiter ID is required")
    private UUID postedBy;

    @NotBlank(message = "Job description is required")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    private String description;
}