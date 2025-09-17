package com.example.satellite.payload;

// dto/TestContentDto.java


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TestContentDTO {
    public UUID testId;
    public String title;
    public Integer timeLimitSec;   // optional; remove if you don’t need it
    public Instant startedAt;
    public Instant endsAt;
    public List<Item> items;

    public static class Item {
        public int position;
        public QuestionOptionDTO question; // <-- your QuestionOptionDto
    }
}
