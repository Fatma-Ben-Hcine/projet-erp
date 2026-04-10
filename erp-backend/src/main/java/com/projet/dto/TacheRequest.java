package com.projet.dto;

import com.projet.enums.StatutTache;
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
public class TacheRequest {

    @NotBlank(message = "Le nom de la tâche est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @NotNull(message = "L'ID de l'activité est obligatoire")
    @Positive(message = "L'ID de l'activité doit être positif")
    private Long activiteId;

    private List<Long> employeIds;
    
    private List<EmployeTacheRequest> employeTaches;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeTacheRequest {
        private Long employeId;
        private StatutTache statut = StatutTache.A_FAIRE;
        private LocalDate dateDebut;
        private LocalDate dateFinReelle;
    }
}
