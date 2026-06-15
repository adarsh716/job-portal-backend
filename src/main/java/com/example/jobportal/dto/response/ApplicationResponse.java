package com.example.jobportal.dto.response;

import com.example.jobportal.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private UUID id;
    private UUID jobId;
    private UUID seekerId;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeUrl;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private String jobTitle;
    private String companyName;
    private String seekerName;
    private String seekerEmail;
}
