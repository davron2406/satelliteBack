package com.example.satellite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name="answers",
        indexes=@Index(name="idx_answers_question", columnList="question_id"),
        uniqueConstraints=@UniqueConstraint(name="uk_answer_question_position", columnNames={"question_id","position"})
)
public class Answer extends AbstractEntity  {


    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="question_id", nullable=false)
    private Question question;

    @Column(nullable=false) private short position = 0;

    @Basic(fetch=FetchType.LAZY) private String text;   // optional if image only
    @Column(nullable=false) private boolean correct = false;

    // --- IMAGE IN DB ---
    @Column(columnDefinition = "text")
    private String imageBase64;          // <-- Base64-encoded image

    @Column(length = 180)
    private String imageContentType;

    @Column(length = 255)
    private String imageFilename;



    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist public void onCreate(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  public void onUpdate(){ updatedAt = Instant.now(); }


}
