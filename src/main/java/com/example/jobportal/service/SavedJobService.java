package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobResponse;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.model.SavedJob;
import com.example.jobportal.model.SavedJobId;
import com.example.jobportal.repository.JobRepository;
import com.example.jobportal.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobService jobService;

    @Transactional
    public void saveJob(UUID userId, UUID jobId) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (savedJobRepository.existsByIdUserIdAndIdJobId(userId, jobId)) {
            throw new RuntimeException("Job already saved");
        }

        SavedJob savedJob = SavedJob.builder()
                .id(new SavedJobId(userId, jobId))
                .savedAt(LocalDateTime.now())
                .build();
        savedJobRepository.save(savedJob);
    }

    @Transactional
    public void unsaveJob(UUID userId, UUID jobId) {
        savedJobRepository.deleteByIdUserIdAndIdJobId(userId, jobId);
    }

    public List<JobResponse> getSavedJobs(UUID userId) {
        return savedJobRepository.findByIdUserId(userId).stream()
                .map(sj -> jobService.toJobResponse(
                        jobRepository.findById(sj.getId().getJobId())
                                .orElseThrow(() -> new ResourceNotFoundException("Job not found"))))
                .toList();
    }

    public boolean isJobSaved(UUID userId, UUID jobId) {
        return savedJobRepository.existsByIdUserIdAndIdJobId(userId, jobId);
    }
}
