package com.example.satellite.repository;


import com.example.satellite.entity.PracticeTemplateLineTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeTemplateLineTopicRepository extends JpaRepository<PracticeTemplateLineTopic, UUID> {
    List<PracticeTemplateLineTopic> findByLineId(UUID lineId);
}

