package com.example.satellite.payload;



import java.time.Instant;
import java.util.UUID;

public class StartTestResponse {
    public UUID testId;
    public Integer timeLimitSec;
    public Instant startedAt;
    public Instant endsAt;
    public int totalQuestions;

}

