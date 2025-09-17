package com.example.satellite.payload;


import lombok.Data;

import java.util.UUID;

@Data
public class AnswerDTO {
    public UUID id;              // optional (for update), omit to create
    public String text;
    public Boolean correct;      // or isCorrect
    public Integer position;     // optional order
    public String imageBase64;   // may be null or "data:...;base64,...."
    public String imageContentType;
    public String imageFilename;

}
