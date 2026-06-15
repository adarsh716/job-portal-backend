package com.example.jobportal.controller;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<Void>> saveJob(@PathVariable UUID jobId, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        savedJobService.saveJob(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success("Job saved", null));
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<Void>> unsaveJob(@PathVariable UUID jobId, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        savedJobService.unsaveJob(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success("Job removed from saved", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<List<JobResponse>>> getSavedJobs(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(savedJobService.getSavedJobs(userId)));
    }
}
