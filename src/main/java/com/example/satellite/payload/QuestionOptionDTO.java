package com.example.satellite.payload;



import java.util.List;
import java.util.UUID;

public class QuestionOptionDTO {
    public UUID id;
    public String text;
    public String hint;
    public String imageUrl;                   // data URL or null
    public List<AnswerOptionDTO> answers;

    public QuestionOptionDTO(UUID id, String text, String imageUrl, String hint, List<AnswerOptionDTO> answers) {
        this.id = id;
        this.text = text;
        this.imageUrl = imageUrl;
        this.answers = answers;
        this.hint = hint;
    }

}

