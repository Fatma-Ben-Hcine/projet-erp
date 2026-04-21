package com.projet.dto;

import com.projet.enums.StatutTache;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TacheResponse {

    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private ActiviteInfo activite;
    private List<EmployeTacheInfo> employeTaches;
    private Integer progression;
    private Integer nombreEmployesAssignes;
    private Integer nombreEmployesTermines;
    private boolean estDeposé;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiviteInfo {
        private Long id;
        private String nom;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeTacheInfo {
        private Long employeId;
        private String employeNom;
        private String employePrenom;
        private LocalDate dateDebut;
        private LocalDate dateFinReelle;
    }
}
