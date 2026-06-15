package com.example.jobportal.repository;

import com.example.jobportal.model.SavedJob;
import com.example.jobportal.model.SavedJobId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, SavedJobId> {
    List<SavedJob> findByIdUserId(UUID userId);
    boolean existsByIdUserIdAndIdJobId(UUID userId, UUID jobId);

    @Modifying
    @Query("DELETE FROM SavedJob s WHERE s.id.userId = :userId AND s.id.jobId = :jobId")
    void deleteByIdUserIdAndIdJobId(@Param("userId") UUID userId, @Param("jobId") UUID jobId);
}
