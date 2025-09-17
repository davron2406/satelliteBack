package com.example.satellite.repository;



import com.example.satellite.entity.PracticeTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PracticeTestResultRepository extends JpaRepository<PracticeTestResult, UUID> { }
