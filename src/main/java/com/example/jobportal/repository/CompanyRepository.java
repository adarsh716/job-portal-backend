package com.example.jobportal.repository;

import com.example.jobportal.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByOwnerId(UUID ownerId);
    List<Company> findByIsVerifiedFalse();
}
