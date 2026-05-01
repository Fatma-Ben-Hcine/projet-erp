package com.projet.dto;

import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceDisponibleDTO {

    private Long id;
    private String nom;
    private String description;
    private Double prix;
    
    // Statut et situation
    private String statut;          // "ACTIVE" / "NON_ACTIVE"
    private String situation;       // "DEMANDE" / "NON_DEMANDE"
    
    // Dates d'abonnement
    private String dateDebutAbonnement;  // nullable, format "yyyy-MM-dd"
    private String dateFinAbonnement;    // nullable, format "yyyy-MM-dd"
    
    // Informations calculées
    private boolean estAbonne;      // true si dates non nulles
    private boolean dejaDemandeParMoi;   // true si cet employé a déjà demandé
    private int nombreDemandes;     // nombre total de demandes toutes personnes
    
    // Anciens champs pour compatibilité (à supprimer plus tard)
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String employeDemandeur;
    private LocalDateTime dateDemande;
}
