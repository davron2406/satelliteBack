package com.example.satellite.repository;


import com.example.satellite.entity.PracticeTemplateLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeTemplateLineRepository extends JpaRepository<PracticeTemplateLine, UUID> {
}