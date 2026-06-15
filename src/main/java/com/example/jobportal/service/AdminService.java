package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.Company;
import com.example.jobportal.model.User;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobService jobService;

    public Page<User> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional
    public void banUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    public Page<Company> getAllCompanies(int page, int size) {
        return companyRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional
    public void verifyCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setVerified(true);
        companyRepository.save(company);
    }

    public Page<JobResponse> getAllJobs(int page, int size) {
        return jobRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(jobService::toJobResponse);
    }

    @Transactional
    public void deleteJob(UUID jobId) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        jobRepository.deleteById(jobId);
    }

    public Map<String, Long> getStats() {
        long totalUsers = userRepository.count();
        long totalJobs = jobRepository.count();
        long totalApplications = applicationRepository.count();
        long totalCompanies = companyRepository.count();
        long activeJobsCount = jobRepository.findByIsActiveTrue(PageRequest.of(0, 1)).getTotalElements();

        return Map.of(
                "totalUsers", totalUsers,
                "totalJobs", totalJobs,
                "totalApplications", totalApplications,
                "totalCompanies", totalCompanies,
                "activeJobsCount", activeJobsCount
        );
    }
}
