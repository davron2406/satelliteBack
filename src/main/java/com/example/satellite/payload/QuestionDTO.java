package com.example.satellite.payload;


import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    public String text;
    public String difficulty;     // or enum name
    public String subject;        // if you link by subject/topic
    public String topic;
    public int point;
    public String hint;
    public String solution;
    public String imageBase64;    // may be data URL or pure Base64 or null
    public String imageContentType;
    public String imageFilename;

    public List<AnswerDTO> answers;
}
