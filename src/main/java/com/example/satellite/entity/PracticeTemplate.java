package com.example.satellite.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "practice_templates")
public class PracticeTemplate extends AbstractEntity {


    @NotBlank @Column(nullable=false, length=160, unique = true)
    private String title;

    @Min(60) @Column(nullable=false)
    private Integer timeLimitSec = 900;

    @Column(nullable=false) private boolean shuffle = true;
    @Column(nullable=false) private boolean allowPartial = false;

    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist public void onCreate(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  public void onUpdate(){ updatedAt = Instant.now(); }

    @OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PracticeTemplateLine> practiceTemplateLines;
}
