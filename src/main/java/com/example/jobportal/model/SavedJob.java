package com.example.jobportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "saved_jobs")
public class SavedJob {

    @EmbeddedId
    private SavedJobId id;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;
}
