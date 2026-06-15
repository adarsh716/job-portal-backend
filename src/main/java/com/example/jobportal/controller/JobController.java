package com.example.jobportal.controller;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.Role;
import com.example.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType type,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) Integer experienceMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                jobService.searchJobs(search, location, type, salaryMin, salaryMax, experienceMin, page, size, sort)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getJobById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<JobResponse>> createJob(@RequestBody JobRequest request, Authentication auth) {
        UUID employerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Job created", jobService.createJob(request, employerId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<JobResponse>> updateJob(@PathVariable UUID id,
                                                        @RequestBody JobRequest request,
                                                        Authentication auth) {
        UUID employerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Job updated", jobService.updateJob(id, request, employerId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable UUID id, Authentication auth) {
        UUID requesterId = UUID.fromString(auth.getName());
        String roleStr = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Role role = Role.valueOf(roleStr);
        jobService.deleteJob(id, requesterId, role);
        return ResponseEntity.ok(ApiResponse.success("Job deleted", null));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<List<JobResponse>>> getMyJobs(Authentication auth) {
        UUID employerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(jobService.getMyJobs(employerId)));
    }
}
