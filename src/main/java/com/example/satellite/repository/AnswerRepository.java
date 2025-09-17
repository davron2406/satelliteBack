package com.example.satellite.repository;


import com.example.satellite.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findByQuestionIdOrderByPositionAsc(UUID questionId);
    long countByQuestionId(UUID questionId);
    boolean existsByQuestionIdAndPosition(UUID questionId, short position);

    List<Answer> findByIdIn(List<UUID> answerIds);
}
