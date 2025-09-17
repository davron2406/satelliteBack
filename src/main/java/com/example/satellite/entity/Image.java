package com.example.satellite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "images")
public class Image extends AbstractEntity{


    private String fileName;
    private String fileType;

    @Column( columnDefinition = "TEXT")
    private String fileUrl;

}
