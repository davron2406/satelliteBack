package com.example.satellite.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "practice_template_line_topics",
        uniqueConstraints = @UniqueConstraint(name="uk_tpl_line_topic", columnNames={"line_id","topic_id"}))
public class PracticeTemplateLineTopic extends AbstractEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="line_id", nullable=false)
    private PracticeTemplateLine line;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="topic_id", nullable=false)
    private Topic topic;

}

