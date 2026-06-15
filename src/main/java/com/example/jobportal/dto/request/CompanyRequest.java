package com.example.jobportal.dto.request;

import lombok.Data;

@Data
public class CompanyRequest {
    private String name;
    private String website;
    private String industry;
    private String size;
    private String description;
    private String location;
}
