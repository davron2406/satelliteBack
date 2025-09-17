package com.example.satellite.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "practice_test_result",
        indexes = {
                @Index(name="idx_result_user_template_time", columnList="userId,templateId,submittedAt")
        })
public class PracticeTestResult {

    @Id
    @Column(nullable=false, updatable=false)
    private UUID id;

    @Column(nullable=false)
    private UUID userId;

    @Column(nullable=false)
    private UUID templateId;

    @Column(nullable = false)
    private UUID practiceTestId;

    @Column(nullable=false)
    private Integer correctCount;

    @Column(nullable=false)
    private Integer totalCount;

    @Column(nullable=false)
    private OffsetDateTime submittedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (submittedAt == null) submittedAt = OffsetDateTime.now();
    }

}
