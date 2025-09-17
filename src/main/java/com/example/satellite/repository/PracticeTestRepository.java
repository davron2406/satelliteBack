package com.example.satellite.repository;




import com.example.satellite.entity.PracticeTemplate;
import com.example.satellite.entity.PracticeTest;
import com.example.satellite.entity.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PracticeTestRepository extends JpaRepository<PracticeTest, UUID> {
    List<PracticeTest> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<PracticeTest> findByUserIdAndStatus(UUID userId, AttemptStatus status);

    Optional<PracticeTest> getByUserIdAndPracticeTemplate(UUID userId, PracticeTemplate practiceTemplate);

}


