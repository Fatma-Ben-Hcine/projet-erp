package com.projet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRessourceRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    private String description;
    
    private String type;
}
