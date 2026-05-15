package com.hireconnect.profile.repository;

import com.hireconnect.profile.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {

    Optional<CandidateProfile> findByUserId(UUID userId);

    Optional<CandidateProfile> findByEmail(String email);

    Optional<CandidateProfile> findByMobile(String mobile);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}