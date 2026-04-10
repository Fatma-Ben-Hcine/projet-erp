package com.projet.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjetRequest {

    @NotBlank(message = "Le nom du projet est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "Le budget est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget doit être supérieur à 0")
    private BigDecimal budget;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être aujourd'hui ou dans le futur")
    private LocalDate dateDebut;

    @NotNull(message = "La date limite est obligatoire")
    @Future(message = "La date limite doit être dans le futur")
    private LocalDate dateLimite;

    @Min(value = 0, message = "La progression ne peut être inférieure à 0")
    @Max(value = 100, message = "La progression ne peut dépasser 100")
    private Integer progression = 0; // Valeur par défaut pour les nouveaux projets

    private List<Long> employeIds;
    private Long chefDeProjetId;
    private Long clientId;
}
