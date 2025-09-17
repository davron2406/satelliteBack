package com.example.satellite.payload;

// dto/CreateClassRequest.java


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public class CreateClassRequest {
    @NotBlank
    public String name;

    @NotEmpty
    public Set<UUID> teacherIds;

    public Set<UUID> studentIds;
}
