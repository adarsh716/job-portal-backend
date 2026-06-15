package com.example.jobportal.dto.request;

import com.example.jobportal.model.ApplicationStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private ApplicationStatus status;
}
