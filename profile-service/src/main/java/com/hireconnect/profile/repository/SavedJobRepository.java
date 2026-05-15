package com.hireconnect.profile.repository;

import com.hireconnect.profile.entity.SavedJob;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {

    List<SavedJob> findByCandidateId(UUID candidateId);

    Optional<SavedJob> findByCandidateIdAndJobId(UUID candidateId, Long jobId);

    @Modifying
    @Transactional
    void deleteByCandidateIdAndJobId(UUID candidateId, Long jobId);

    boolean existsByCandidateIdAndJobId(UUID candidateId, Long jobId);
}
