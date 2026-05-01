package com.projet.dto;

import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceResponse {
    private Long id;
    private String nom;
    private String description;
    private SituationRessource situation;
    private StatutRessource statut;
    private Double prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String employeDemandeurNom; // prénom + nom si demandé
    private LocalDateTime dateDemande;
    private boolean dejaDemandeParMoi;  // pour l'interface employé
    private int nombreDemandes;        // nombre total de demandes
}
