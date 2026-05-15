package com.hireconnect.profile.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CandidateProfile extends UserProfile {

    private String location;

    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "candidate_profile_skills",
            joinColumns = @JoinColumn(name = "candidate_profile_profile_id")
    )
    @Column(name = "skills")
    private List<String> skills;

    private Integer experience;

    private LocalDate dob;

    private String gender;

    private String resumeUrl;

    private String resumeName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "candidate_profiles_addresses",
            joinColumns = @JoinColumn(name = "candidate_profile_profile_id")
    )
    private List<Address> addresses;
}