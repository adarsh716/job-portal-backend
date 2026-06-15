package com.example.jobportal.controller;

import com.example.jobportal.dto.request.ApplicationRequest;
import com.example.jobportal.dto.request.StatusUpdateRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.ApplicationResponse;
import com.example.jobportal.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<ApplicationResponse>> apply(@PathVariable UUID jobId,
                                                            @RequestBody ApplicationRequest request,
                                                            Authentication auth) {
        UUID seekerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Application submitted",
                applicationService.apply(jobId, request, seekerId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(Authentication auth) {
        UUID seekerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(applicationService.getMyApplications(seekerId)));
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<ApplicationResponse>> withdraw(@PathVariable UUID id, Authentication auth) {
        UUID seekerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn",
                applicationService.withdraw(id, seekerId)));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<List<ApplicationResponse>>> getJobApplicants(@PathVariable UUID jobId,
                                                                              Authentication auth) {
        UUID employerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(applicationService.getJobApplicants(jobId, employerId)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(@PathVariable UUID id,
                                                                    @RequestBody StatusUpdateRequest request,
                                                                    Authentication auth) {
        UUID employerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                applicationService.updateStatus(id, request.getStatus(), employerId)));
    }
}
