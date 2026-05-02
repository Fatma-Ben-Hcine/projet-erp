package com.projet.dto;

import com.projet.enums.StatutActivite;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteResponse {

    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private ProjetInfo projet;
    private List<TacheInfo> taches;
    private List<EmployeActiviteInfo> employeActivites;
    private Integer progressionMoyenne;
    private Integer nombreEmployesAssignes;
    private boolean estDeposé;
    private List<DepotResponse> depots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjetInfo {
        private Long id;
        private String nom;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiviteInfo {
        private Long id;
        private String nom;
        private String description;
        private Long projetId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TacheInfo {
        private Long id;
        private String nom;
        private String description;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private Integer progression;
        private boolean estDeposé;
        private List<EmployeTacheInfo> employeTaches;
        private List<DepotResponse> depots;
        private ActiviteInfo activite;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeTacheInfo {
        private Long employeId;
        private String employeNom;
        private String employePrenom;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String poste;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeActiviteInfo {
        private Long employeId;
        private String employeNom;
        private String employePrenom;
        private Integer progression;
    }
}
