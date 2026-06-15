package com.example.jobportal.repository;

import com.example.jobportal.model.SeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeekerProfileRepository extends JpaRepository<SeekerProfile, UUID> {
    Optional<SeekerProfile> findByUserId(UUID userId);
}
