package com.projet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    private String description;
    
    private Double prix;
    
    private LocalDate dateDebutAbonnement;
    
    private LocalDate dateFinAbonnement;
    
    // statut géré par la logique métier
    // pas dans le request de création
}
