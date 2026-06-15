package com.example.jobportal.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String location;
    private String bio;
    private String linkedinUrl;
    private String githubUrl;
    private List<String> skills;
    private Integer experienceYears;
    private String education;
}
