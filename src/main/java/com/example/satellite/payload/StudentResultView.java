package com.example.satellite.payload;


import java.time.OffsetDateTime;
import java.util.UUID;

public interface StudentResultView {
    UUID getStudentId();
    String getFirstName();
    String getLastName();

    UUID getResultId();
    Integer getCorrectCount();
    Integer getTotalQuestions();
    Double getScore();         // or BigDecimal if you use it
    OffsetDateTime getStartedAt();
    OffsetDateTime getFinishedAt();
}
