package com.example.jobportal.service;

import com.example.jobportal.dto.request.JobRequest;
import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.exception.UnauthorizedException;
import com.example.jobportal.model.Company;
import com.example.jobportal.model.Job;
import com.example.jobportal.model.JobType;
import com.example.jobportal.model.Role;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;

    public Page<JobResponse> searchJobs(String search, String location, JobType type,
                                         Integer salaryMin, Integer salaryMax, Integer experienceMin,
                                         int page, int size, String sort) {
        Sort sorting = Sort.by("createdAt").descending();
        if ("salary".equals(sort)) sorting = Sort.by("salaryMax").descending();
        if ("oldest".equals(sort)) sorting = Sort.by("createdAt").ascending();

        Pageable pageable = PageRequest.of(page, size, sorting);

        Specification<Job> spec = (root, query, cb) -> cb.isTrue(root.get("isActive"));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), cb.literal(pattern)),
                    cb.like(cb.lower(root.get("description")), cb.literal(pattern))
            ));
        }

        if (location != null && !location.isBlank()) {
            String pattern = "%" + location.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")), cb.literal(pattern)));
        }

        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }

        if (salaryMin != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("salaryMin"), salaryMin));
        }

        if (salaryMax != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("salaryMax"), salaryMax));
        }

        if (experienceMin != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("experienceMin"), experienceMin));
        }

        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        Map<UUID, Company> companies = batchLoadCompanies(jobPage.getContent());
        Map<UUID, Long> counts = batchLoadCounts(jobPage.getContent());
        return jobPage.map(job -> toJobResponse(job, companies, counts));
    }

    public JobResponse getJobById(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return toJobResponse(job);
    }

    @Transactional
    public JobResponse createJob(JobRequest request, UUID employerId) {
        companyRepository.findByOwnerId(employerId)
                .orElseThrow(() -> new RuntimeException("Please set up your company profile before posting jobs"));

        UUID companyId = companyRepository.findByOwnerId(employerId).get().getId();

        Job job = Job.builder()
                .companyId(companyId)
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .type(request.getType())
                .location(request.getLocation())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .skillsRequired(request.getSkillsRequired() != null
                        ? request.getSkillsRequired().toArray(new String[0]) : new String[0])
                .experienceMin(request.getExperienceMin())
                .deadline(request.getDeadline())
                .build();

        return toJobResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse updateJob(UUID jobId, JobRequest request, UUID employerId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        UUID companyId = companyRepository.findByOwnerId(employerId)
                .orElseThrow(() -> new UnauthorizedException("Company not found")).getId();

        if (!job.getCompanyId().equals(companyId)) {
            throw new UnauthorizedException("Not authorized to update this job");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setType(request.getType());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setSkillsRequired(request.getSkillsRequired() != null
                ? request.getSkillsRequired().toArray(new String[0]) : new String[0]);
        job.setExperienceMin(request.getExperienceMin());
        job.setDeadline(request.getDeadline());

        return toJobResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(UUID jobId, UUID requesterId, Role role) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (role == Role.EMPLOYER) {
            UUID companyId = companyRepository.findByOwnerId(requesterId)
                    .orElseThrow(() -> new UnauthorizedException("Company not found")).getId();
            if (!job.getCompanyId().equals(companyId)) {
                throw new UnauthorizedException("Not authorized to delete this job");
            }
        }

        jobRepository.delete(job);
    }

    public List<JobResponse> getMyJobs(UUID employerId) {
        UUID companyId = companyRepository.findByOwnerId(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found")).getId();
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        Map<UUID, Company> companies = batchLoadCompanies(jobs);
        Map<UUID, Long> counts = batchLoadCounts(jobs);
        return jobs.stream().map(job -> toJobResponse(job, companies, counts)).toList();
    }

    private Map<UUID, Company> batchLoadCompanies(List<Job> jobs) {
        Set<UUID> ids = jobs.stream().map(Job::getCompanyId).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return companyRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Company::getId, c -> c));
    }

    private Map<UUID, Long> batchLoadCounts(List<Job> jobs) {
        List<UUID> ids = jobs.stream().map(Job::getId).toList();
        if (ids.isEmpty()) return Map.of();
        Map<UUID, Long> result = new HashMap<>();
        applicationRepository.countsByJobIds(ids)
                .forEach(v -> result.put(v.getJobId(), v.getCount()));
        return result;
    }

    // Used for single-job responses (getJobById, create, update)
    JobResponse toJobResponse(Job job) {
        Company company = companyRepository.findById(job.getCompanyId()).orElse(null);
        String companyName = company != null ? company.getName() : "Unknown";
        String companyLogo = company != null ? company.getLogoUrl() : null;
        Long applicantCount = applicationRepository.countByJobId(job.getId());
        return buildResponse(job, companyName, companyLogo, applicantCount);
    }

    // Used for list/page responses — maps are pre-loaded to avoid N+1
    private JobResponse toJobResponse(Job job, Map<UUID, Company> companies, Map<UUID, Long> counts) {
        Company company = companies.get(job.getCompanyId());
        String companyName = company != null ? company.getName() : "Unknown";
        String companyLogo = company != null ? company.getLogoUrl() : null;
        Long applicantCount = counts.getOrDefault(job.getId(), 0L);
        return buildResponse(job, companyName, companyLogo, applicantCount);
    }

    private JobResponse buildResponse(Job job, String companyName, String companyLogo, Long applicantCount) {
        return JobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .type(job.getType())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skillsRequired(job.getSkillsRequired() != null
                        ? Arrays.asList(job.getSkillsRequired()) : List.of())
                .experienceMin(job.getExperienceMin())
                .isActive(job.isActive())
                .deadline(job.getDeadline())
                .createdAt(job.getCreatedAt())
                .companyName(companyName)
                .companyLogo(companyLogo)
                .applicantCount(applicantCount)
                .build();
    }
}
