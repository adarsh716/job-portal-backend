package com.example.jobportal.dto.response;

import com.example.jobportal.model.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private UUID id;
    private UUID companyId;
    private String title;
    private String description;
    private String requirements;
    private JobType type;
    private String location;
    private Integer salaryMin;
    private Integer salaryMax;
    private List<String> skillsRequired;
    private Integer experienceMin;
    private boolean isActive;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private String companyName;
    private String companyLogo;
    private Long applicantCount;
}
