package com.example.jobportal.service;

import com.example.jobportal.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;

    public String uploadFile(MultipartFile file, String bucket, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadUrl = appProperties.getSupabase().getUrl()
                + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + appProperties.getSupabase().getServiceRoleKey());
        headers.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        try {
            restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, Map.class);
        } catch (Exception e) {
            log.error("Failed to upload file to Supabase: {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }

        if (bucket.equals(appProperties.getSupabase().getStorageBucketLogos())) {
            return appProperties.getSupabase().getUrl()
                    + "/storage/v1/object/public/" + bucket + "/" + fileName;
        }

        return getSignedUrl(bucket, fileName);
    }

    private String getSignedUrl(String bucket, String path) {
        String signUrl = appProperties.getSupabase().getUrl()
                + "/storage/v1/object/sign/" + bucket + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + appProperties.getSupabase().getServiceRoleKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("expiresIn", 31536000), headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(signUrl, HttpMethod.POST, entity, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("signedURL")) {
                return appProperties.getSupabase().getUrl()
                        + "/storage/v1" + response.getBody().get("signedURL");
            }
        } catch (Exception e) {
            log.error("Failed to get signed URL: {}", e.getMessage());
        }

        return appProperties.getSupabase().getUrl()
                + "/storage/v1/object/" + bucket + "/" + path;
    }
}
