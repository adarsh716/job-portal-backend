package com.example.jobportal.repository;

import com.example.jobportal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findBySeekerId(UUID seekerId);
    List<Application> findByJobId(UUID jobId);
    Optional<Application> findByJobIdAndSeekerId(UUID jobId, UUID seekerId);
    boolean existsByJobIdAndSeekerId(UUID jobId, UUID seekerId);
    Long countByJobId(UUID jobId);

    @Query("SELECT a.jobId AS jobId, COUNT(a.id) AS count FROM Application a WHERE a.jobId IN :jobIds GROUP BY a.jobId")
    List<JobCountView> countsByJobIds(@Param("jobIds") Collection<UUID> jobIds);

    interface JobCountView {
        UUID getJobId();
        Long getCount();
    }
}
