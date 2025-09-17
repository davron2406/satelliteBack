package com.example.satellite.payload;



import java.util.List;
import java.util.UUID;

public class CreateTemplateRequest {
    public String title;
    public Integer timeLimitSec;
    public Boolean shuffle;
    public Boolean allowPartial;
    public List<Line> lines;

    public static class Line {
        public String difficulty;   // EASY/MEDIUM/HARD
        public Integer count;       // >= 0
        public UUID topicId; // >= 1
    }
}

