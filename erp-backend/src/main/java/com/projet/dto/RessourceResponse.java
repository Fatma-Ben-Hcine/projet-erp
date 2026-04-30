package com.projet.dto;

import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceResponse {

    private Long id;
    private String nom;
    private String description;
    private SituationRessource situation;
    private StatutRessource statut;
    private BigDecimal prix;
    private LocalDate dateDebutAbonnement;
    private LocalDate dateFinAbonnement;
    private boolean estAbonne;
    private boolean statutForceManuel;

    // Informations calculées pour le frontend
    private boolean abonnementExpire;
    private boolean abonnementEnCours;
    private boolean abonnementFutur;

    // Informations du projet associé
    private ProjetInfo projet;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjetInfo {
        private Long id;
        private String nom;
    }
}
