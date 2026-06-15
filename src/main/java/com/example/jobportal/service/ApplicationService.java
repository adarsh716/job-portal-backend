package com.example.jobportal.service;

import com.example.jobportal.dto.request.ApplicationRequest;
import com.example.jobportal.dto.response.ApplicationResponse;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.exception.UnauthorizedException;
import com.example.jobportal.model.Application;
import com.example.jobportal.model.ApplicationStatus;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.SeekerProfileRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public ApplicationResponse apply(UUID jobId, ApplicationRequest request, UUID seekerId) {
        if (applicationRepository.existsByJobIdAndSeekerId(jobId, seekerId)) {
            throw new RuntimeException("You have already applied to this job");
        }

        jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        Application application = Application.builder()
                .jobId(jobId)
                .seekerId(seekerId)
                .coverLetter(request.getCoverLetter())
                .resumeUrl(request.getResumeUrl())
                .build();

        application = applicationRepository.save(application);

        jobRepository.findById(jobId).ifPresent(job ->
            companyRepository.findById(job.getCompanyId()).ifPresent(company ->
                userRepository.findById(company.getOwnerId()).ifPresent(owner ->
                    notificationService.createNotification(
                        owner.getId(),
                        "New application received for: " + job.getTitle(),
                        "NEW_APPLICANT"
                    )
                )
            )
        );

        return toApplicationResponse(application);
    }

    public List<ApplicationResponse> getMyApplications(UUID seekerId) {
        return applicationRepository.findBySeekerId(seekerId).stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse withdraw(UUID applicationId, UUID seekerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getSeekerId().equals(seekerId)) {
            throw new UnauthorizedException("Not authorized to withdraw this application");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        return toApplicationResponse(applicationRepository.save(application));
    }

    public List<ApplicationResponse> getJobApplicants(UUID jobId, UUID employerId) {
        UUID companyId = companyRepository.findByOwnerId(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found")).getId();

        jobRepository.findById(jobId)
                .filter(j -> j.getCompanyId().equals(companyId))
                .orElseThrow(() -> new UnauthorizedException("Not authorized to view applicants for this job"));

        return applicationRepository.findByJobId(jobId).stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(UUID applicationId, ApplicationStatus status, UUID employerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        UUID companyId = companyRepository.findByOwnerId(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found")).getId();

        jobRepository.findById(application.getJobId())
                .filter(j -> j.getCompanyId().equals(companyId))
                .orElseThrow(() -> new UnauthorizedException("Not authorized to update this application"));

        application.setStatus(status);
        application = applicationRepository.save(application);

        String message = buildStatusMessage(status,
                jobRepository.findById(application.getJobId()).map(j -> j.getTitle()).orElse("a job"));
        notificationService.createNotification(application.getSeekerId(), message, "APPLICATION_UPDATE");

        return toApplicationResponse(application);
    }

    private String buildStatusMessage(ApplicationStatus status, String jobTitle) {
        return switch (status) {
            case SHORTLISTED -> "Your application for '" + jobTitle + "' has been shortlisted!";
            case INTERVIEW -> "You've been invited for an interview for '" + jobTitle + "'!";
            case OFFERED -> "Congratulations! You've received a job offer for '" + jobTitle + "'!";
            case REJECTED -> "Your application for '" + jobTitle + "' was not selected.";
            default -> "Your application status for '" + jobTitle + "' has been updated to " + status.name();
        };
    }

    private ApplicationResponse toApplicationResponse(Application application) {
        String jobTitle = jobRepository.findById(application.getJobId())
                .map(j -> j.getTitle()).orElse("Unknown");
        String companyName = jobRepository.findById(application.getJobId())
                .flatMap(j -> companyRepository.findById(j.getCompanyId()))
                .map(c -> c.getName()).orElse("Unknown");
        String seekerName = seekerProfileRepository.findByUserId(application.getSeekerId())
                .map(p -> p.getFullName()).orElse(null);
        String seekerEmail = userRepository.findById(application.getSeekerId())
                .map(u -> u.getEmail()).orElse(null);

        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .seekerId(application.getSeekerId())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .jobTitle(jobTitle)
                .companyName(companyName)
                .seekerName(seekerName)
                .seekerEmail(seekerEmail)
                .build();
    }
}
