package com.example.satellite.entity;

import com.example.satellite.entity.enums.Difficulty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name="questions",
        indexes={@Index(name="idx_questions_topic", columnList="topic_id"),
                @Index(name="idx_questions_difficulty", columnList="difficulty")}
)
public class Question extends AbstractEntity  {

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="topic_id", nullable=false)
    private Topic topic;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private Difficulty difficulty;

     @NotBlank private String text;
     private String hint; private String solution;

    @NotNull @Min(1) private Integer points;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="question_tags", joinColumns=@JoinColumn(name="question_id"))
    @Column(name="tag", length=64)
    private List<String> tags = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String imageBase64;          // <-- Base64-encoded image

    @Column(length = 180)
    private String imageContentType;

    @Column(length = 255)
    private String imageFilename;

    @OneToMany(mappedBy="question", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    @OrderBy("position ASC, id ASC")
    private List<Answer> answers = new ArrayList<>();

    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    @PrePersist public void onCreate(){ createdAt = updatedAt = Instant.now(); }
    @PreUpdate  public void onUpdate(){ updatedAt = Instant.now(); }

    public void addAnswer(Answer a){ a.setQuestion(this); answers.add(a); }


}