package com.example.satellite.entity;

import com.example.satellite.entity.enums.AttemptStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(
        name = "practice_tests",
        indexes = {
                @Index(name="idx_pt_user", columnList="user_id"),
                @Index(name="idx_pt_template", columnList="practice_template_id") // NEW
        }
)
public class PracticeTest extends AbstractEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @NotBlank @Column(nullable=false, length=160)
    private String title;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user; // the student user taking this attempt

    // NEW: link to the template this practice test is based on
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_template_id", nullable = false)
    private PracticeTemplate practiceTemplate;

    @Min(60) @Column(nullable=false)
    private Integer timeLimitSec = 900;

    @Column private Instant startedAt;
    @Column private Instant endsAt;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private AttemptStatus status = AttemptStatus.ACTIVE;

    @Column(nullable=false) private boolean shuffle = true;
    @Column(nullable=false) private boolean allowPartial = false;

    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist public void onCreate(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  public void onUpdate(){ updatedAt = Instant.now(); }

    // Convenience accessor if you need templateId often:
    public UUID getTemplateId() {
        return practiceTemplate != null ? practiceTemplate.getId() : null;
    }
}
