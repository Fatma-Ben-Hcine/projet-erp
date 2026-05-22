package com.projet.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private long totalProjets;
    private long retard;
    private long termine;
    private long totalActivites;
    private long totalTaches;
    private long totalUtilisateursPermanents;
    private long totalEmployes;
    private long totalAdministrateurs;
    private long totalUtilisateursTemporaires;
}
