package com.projet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRessourceResponse {

    private Long id;
    private LocalDateTime dateDemande;
    private boolean estTraitee;

    // Informations de la ressource
    private RessourceInfo ressource;

    // Informations de l'employé
    private EmployeInfo employe;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RessourceInfo {
        private Long id;
        private String nom;
        private String description;
        private double prix;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeInfo {
        private Long id;
        private String nom;
        private String prenom;
    }
}
