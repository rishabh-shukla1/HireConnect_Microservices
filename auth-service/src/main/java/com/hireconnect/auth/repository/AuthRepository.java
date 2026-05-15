package com.hireconnect.auth.repository;

import com.hireconnect.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<UserCredential, UUID> {

    Optional<UserCredential> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}