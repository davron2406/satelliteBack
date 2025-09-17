package com.example.satellite.repository;


import com.example.satellite.entity.PracticeTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeTestQuestionRepository extends JpaRepository<PracticeTestQuestion, UUID> {
    List<PracticeTestQuestion> findByTestIdOrderByPositionAsc(UUID testId);
    boolean existsByTestIdAndQuestionId(UUID testId, UUID questionId);

    List<PracticeTestQuestion> findQuestionIdsByTestId(UUID practiceTestId);
    int countByTestId(UUID practiceTestId);

    List<PracticeTestQuestion> findAllByTestId(UUID testId);
}

