package com.projet.dto;

import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RessourceRequest {

    @NotBlank(message = "Le nom de la ressource est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Le statut est obligatoire")
    private StatutRessource statut;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal prix;

    // Dates d'abonnement — optionnelles
    private LocalDate dateDebutAbonnement;

    private LocalDate dateFinAbonnement;

    // Pour marquer si le statut a été forcé manuellement
    private Boolean statutForceManuel = false;

    // ID du projet associé (optionnel)
    private Long projetId;
}
