package com.projet.dto;

import com.projet.enums.StatutActivite;
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
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @NotNull(message = "L'ID du projet est obligatoire")
    @Positive(message = "L'ID du projet doit être positif")
    private Long projetId;

    private List<Long> employeIds;
    
    private List<EmployeActiviteRequest> employeActivites;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeActiviteRequest {
        private Long employeId;
        private StatutActivite statut = StatutActivite.EN_COURS;
        private Integer progression = 0;
        private LocalDate dateDebut;
        private LocalDate dateFin;
    }
}
