package com.example.satellite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name="subjects", uniqueConstraints=@UniqueConstraint(name="uk_subject_name", columnNames="name"))
public class Subject extends AbstractEntity {
    @NotBlank @Column(nullable=false, length=120) private String name;
    @Column(length=80) private String code;
    @Column(nullable=false) private boolean active = true;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist public void onCreate(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  public void onUpdate(){ updatedAt = Instant.now(); }

}
