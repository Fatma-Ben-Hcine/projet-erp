package com.projet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceDisponibleDTO {

    private Long id;
    private String nom;
    private String description;
    private String type;
    private BigDecimal prix;
    private String statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statutForceManuel;
    private boolean abonnementEnCours;
    private boolean abonnementFutur;
    private String situation;
    
    // Champ clé pour la logique multi-employés
    private boolean dejaDemandeParMoi;
}
