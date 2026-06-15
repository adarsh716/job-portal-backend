package com.example.jobportal.controller;

import com.example.jobportal.dto.request.CompanyRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.model.Company;
import com.example.jobportal.service.CompanyService;
import com.example.jobportal.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final FileStorageService fileStorageService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<Company>> createCompany(@RequestBody CompanyRequest request, Authentication auth) {
        UUID ownerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Company created", companyService.createCompany(request, ownerId)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<Company>> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompanyById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<Company>> updateCompany(@PathVariable UUID id,
                                                        @RequestBody CompanyRequest request,
                                                        Authentication auth) {
        UUID ownerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Company updated", companyService.updateCompany(id, request, ownerId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<Company>> getMyCompany(Authentication auth) {
        UUID ownerId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(companyService.getMyCompany(ownerId)));
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasRole('EMPLOYER')")
    ResponseEntity<ApiResponse<Company>> uploadLogo(@PathVariable UUID id,
                                                     @RequestParam("file") MultipartFile file,
                                                     Authentication auth) throws Exception {
        UUID ownerId = UUID.fromString(auth.getName());
        String bucket = "logos";
        String logoUrl = fileStorageService.uploadFile(file, bucket, "company-logos");
        return ResponseEntity.ok(ApiResponse.success("Logo uploaded", companyService.updateLogo(id, logoUrl, ownerId)));
    }
}
