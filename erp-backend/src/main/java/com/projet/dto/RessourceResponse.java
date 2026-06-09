package com.projet.dto;

import com.projet.enums.StatutRessource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceResponse {
    private Long id;
    private String nom;
    private String description;
    private StatutRessource statut;
    private Double prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean dejaDemandeParMoi;  // pour l'interface employé
    private int nombreDemandes;        // nombre total de demandes
}
