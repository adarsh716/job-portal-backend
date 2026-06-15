package com.example.jobportal.controller;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.model.Company;
import com.example.jobportal.model.User;
import com.example.jobportal.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(page, size)));
    }

    @PutMapping("/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Void>> banUser(@PathVariable UUID id) {
        adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success("User banned", null));
    }

    @PutMapping("/users/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable UUID id) {
        adminService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unbanned", null));
    }

    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Page<Company>>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllCompanies(page, size)));
    }

    @PutMapping("/companies/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Void>> verifyCompany(@PathVariable UUID id) {
        adminService.verifyCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company verified", null));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Page<JobResponse>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllJobs(page, size)));
    }

    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable UUID id) {
        adminService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted", null));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getStats()));
    }
}
