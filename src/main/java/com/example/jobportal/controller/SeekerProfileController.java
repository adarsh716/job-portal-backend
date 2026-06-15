package com.example.jobportal.controller;

import com.example.jobportal.dto.request.ProfileUpdateRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.model.SeekerProfile;
import com.example.jobportal.service.FileStorageService;
import com.example.jobportal.service.SeekerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class SeekerProfileController {

    private final SeekerProfileService seekerProfileService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<SeekerProfile>> getMyProfile(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(seekerProfileService.getMyProfile(userId)));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<SeekerProfile>> updateProfile(@RequestBody ProfileUpdateRequest request,
                                                              Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                seekerProfileService.updateProfile(userId, request)));
    }

    @PostMapping("/me/resume")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    ResponseEntity<ApiResponse<SeekerProfile>> uploadResume(@RequestParam("file") MultipartFile file,
                                                             Authentication auth) throws Exception {
        UUID userId = UUID.fromString(auth.getName());
        String resumeUrl = fileStorageService.uploadFile(file, "resumes", "resumes");
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded",
                seekerProfileService.updateResumeUrl(userId, resumeUrl)));
    }
}
