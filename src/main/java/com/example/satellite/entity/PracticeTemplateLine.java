package com.example.satellite.entity;


import com.example.satellite.entity.enums.Difficulty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "practice_template_lines")
public class PracticeTemplateLine extends AbstractEntity {


    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="template_id", nullable=false)
    private PracticeTemplate template;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private Difficulty difficulty;

    @Min(0) @Column(nullable=false)
    private Integer count;

}

