package com.example.satellite.repository;


import com.example.satellite.entity.Subject;
import com.example.satellite.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    Optional<Topic> findBySubjectAndNameIgnoreCase(Subject subject, String name);
    List<Topic> findBySubjectIdAndActiveTrueOrderByNameAsc(UUID subjectId);
    List<Topic> findBySubjectIdAndNameContainingIgnoreCaseAndActiveTrueOrderByNameAsc(UUID subjectId, String q);
}


