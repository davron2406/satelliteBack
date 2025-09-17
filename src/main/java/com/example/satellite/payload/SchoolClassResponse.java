package com.example.satellite.payload;



import java.util.List;
import java.util.UUID;

public class SchoolClassResponse {
    public UUID id;
    public String name;
    public List<UserSummary> teachers;
    public int studentCount;

    public SchoolClassResponse(UUID id, String name, List<UserSummary> teachers, int studentCount) {
        this.id = id; this.name = name; this.teachers = teachers; this.studentCount = studentCount;
    }
}

