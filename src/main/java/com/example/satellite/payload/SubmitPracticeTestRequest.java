package com.example.satellite.payload;


import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;
import java.util.UUID;

public class SubmitPracticeTestRequest {
    public UUID practiceTestId;              // comes from frontend
    public List<Item> answers;               // list of selected pairs

    public static class Item {
        public UUID questionId;
        @JsonAlias({"selectedAnswerId"})     // accept either answerId or selectedAnswerId
        public UUID answerId;
    }
}
