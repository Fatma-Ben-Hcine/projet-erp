package com.projet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteRequest {

    @NotBlank(message = "Le nom de l'activité est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFin;

    @Positive(message = "L'ID du projet doit être positif")
    private Long projetId; // Optionnel lors de la création avec le projet

    private Boolean estDeposé = false; // Valeur par défaut pour les nouvelles activités

    private List<Long> employeIds;

    private List<EmployeActiviteRequest> employeActivites;

    private List<TacheRequestSimple> taches;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeActiviteRequest {
        private Long employeId;
        private Integer progression = 0;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDebut;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TacheRequestSimple {
        @NotBlank(message = "Le nom de la tâche est obligatoire")
        private String nom;

        private String description;

        @NotNull(message = "La date de début de la tâche est obligatoire")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateDebut;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFin;

        private List<Long> employeIds;
    }
}
