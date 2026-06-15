package com.example.jobportal.dto.request;

import com.example.jobportal.model.JobType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JobRequest {
    private String title;
    private String description;
    private String requirements;
    private JobType type;
    private String location;
    private Integer salaryMin;
    private Integer salaryMax;
    private List<String> skillsRequired;
    private Integer experienceMin;
    private LocalDate deadline;
}
