package com.example.satellite.payload;


import lombok.Data;

import java.util.UUID;

@Data
public class OptionDTO { public UUID id; public String name;
    public OptionDTO(UUID id, String name){ this.id=id; this.name=name; } }

