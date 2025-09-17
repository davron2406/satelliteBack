package com.example.satellite.payload;



import java.util.List;

public class SubmitTestRequest {
    public List<Answer> answers;
    public static class Answer {
        public Long questionId;
        public List<Long> selectedAnswerIds; // MCQ
        public String text;                  // open-ended
    }
}
