package com.projet.dto;

import com.projet.enums.StatutDemande;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRessourceResponse {

    private Long id;
    private LocalDateTime dateDemande;
    private StatutDemande statutDemande;

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
        private Double prix;
        private LocalDate dateDebut;
        private LocalDate dateFin;
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
