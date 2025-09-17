package com.example.satellite.payload;



import java.util.UUID;

public class UserSummary {
    public UUID id;
    public String fullName;
    public String email;
    public String role;

    public UserSummary(UUID id, String fullName, String email, String role) {
        this.id = id; this.fullName = fullName; this.email = email; this.role = role;
    }
}

