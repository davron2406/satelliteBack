package com.example.satellite.payload;


import java.util.UUID;

public class AnswerOptionDTO {
    public UUID id;
    public String text;
    public String imageUrl;     // data URL or null

    public AnswerOptionDTO(UUID id, String text, String imageUrl) {
        this.id = id;
        this.text = text;
        this.imageUrl = imageUrl;
    }

}