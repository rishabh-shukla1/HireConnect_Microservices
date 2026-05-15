package com.hireconnect.profile.repository;

import com.hireconnect.profile.entity.RecruiterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, UUID> {

    Optional<RecruiterProfile> findByUserId(UUID userId);

    Optional<RecruiterProfile> findByEmail(String email);

    Optional<RecruiterProfile> findByMobile(String mobile);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}