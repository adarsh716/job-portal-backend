package com.example.jobportal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Supabase supabase = new Supabase();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
        private long refreshExpirationMs;
    }

    @Data
    public static class Supabase {
        private String url;
        private String serviceRoleKey;
        private String storageBucketResumes;
        private String storageBucketLogos;
    }
}
