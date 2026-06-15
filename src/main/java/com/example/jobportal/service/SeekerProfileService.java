package com.example.jobportal.service;

import com.example.jobportal.dto.request.ProfileUpdateRequest;
import com.example.jobportal.model.SeekerProfile;
import com.example.jobportal.repository.SeekerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeekerProfileService {

    private final SeekerProfileRepository seekerProfileRepository;

    public SeekerProfile getMyProfile(UUID userId) {
        return seekerProfileRepository.findByUserId(userId).orElseGet(() -> {
            SeekerProfile profile = SeekerProfile.builder().userId(userId).build();
            return seekerProfileRepository.save(profile);
        });
    }

    @Transactional
    public SeekerProfile updateProfile(UUID userId, ProfileUpdateRequest request) {
        SeekerProfile profile = seekerProfileRepository.findByUserId(userId).orElseGet(() ->
                SeekerProfile.builder().userId(userId).build());

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setEducation(request.getEducation());

        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills().toArray(new String[0]));
        }

        return seekerProfileRepository.save(profile);
    }

    @Transactional
    public SeekerProfile updateResumeUrl(UUID userId, String resumeUrl) {
        SeekerProfile profile = seekerProfileRepository.findByUserId(userId).orElseGet(() ->
                SeekerProfile.builder().userId(userId).build());
        profile.setResumeUrl(resumeUrl);
        return seekerProfileRepository.save(profile);
    }
}
