package com.example.satellite.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name="topics",
        indexes = {@Index(name="idx_topics_subject", columnList="subject_id"), @Index(name="idx_topics_parent", columnList="parent_id")},
        uniqueConstraints=@UniqueConstraint(name="uk_topic_subject_name", columnNames={"subject_id","name"})
)
public class Topic extends AbstractEntity {

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="subject_id", nullable=false)
    private Subject subject;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id") private Topic parent;

    @NotBlank @Column(nullable=false, length=160) private String name;
    @Column(nullable=false) private boolean active = true;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist public void onCreate(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  public void onUpdate(){ updatedAt = Instant.now(); }

}
