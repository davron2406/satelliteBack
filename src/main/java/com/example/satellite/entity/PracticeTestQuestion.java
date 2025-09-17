package com.example.satellite.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "practice_test_questions",
        uniqueConstraints = @UniqueConstraint(name="uk_ptq_test_question", columnNames={"test_id","question_id"}),
        indexes = {
                @Index(name="idx_ptq_test", columnList="test_id"),
                @Index(name="idx_ptq_question", columnList="question_id"),
                @Index(name="idx_ptq_pos", columnList="test_id,position")
        })
public class PracticeTestQuestion extends AbstractEntity{

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="test_id", nullable=false)
    private PracticeTest test;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="question_id", nullable=false)
    private Question question;

    @Column(nullable=false) private int position;

    // Optional student response capture
//    @Column(length=800) private String textAnswer;
//    @Column(length=400) private String selectedAnswerIdsCsv;
//    private Boolean correct;

}
