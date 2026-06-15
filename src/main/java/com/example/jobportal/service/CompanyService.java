package com.example.jobportal.service;

import com.example.jobportal.dto.request.CompanyRequest;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.exception.UnauthorizedException;
import com.example.jobportal.model.Company;
import com.example.jobportal.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Company createCompany(CompanyRequest request, UUID ownerId) {
        Company company = Company.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .size(request.getSize())
                .description(request.getDescription())
                .location(request.getLocation())
                .build();
        return companyRepository.save(company);
    }

    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }

    @Transactional
    public Company updateCompany(UUID id, CompanyRequest request, UUID ownerId) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("Not authorized to update this company");
        }

        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setSize(request.getSize());
        company.setDescription(request.getDescription());
        company.setLocation(request.getLocation());

        return companyRepository.save(company);
    }

    public Company getMyCompany(UUID ownerId) {
        return companyRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for this employer"));
    }

    @Transactional
    public Company updateLogo(UUID id, String logoUrl, UUID ownerId) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("Not authorized to update this company");
        }

        company.setLogoUrl(logoUrl);
        return companyRepository.save(company);
    }
}
