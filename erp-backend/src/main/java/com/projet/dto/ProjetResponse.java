package com.projet.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjetResponse {
    private Long id;
    private String nom;
    private String description;
    private BigDecimal budget;
    private LocalDate dateDebut;
    private LocalDate dateLimite;
    private Integer progression;
    private String statut;
    private Integer joursRestants;
    
    // Informations sur le client
    private ClientInfo client;
    
    // Informations sur le chef de projet
    private EmployeInfo chefDeProjet;
    
    // Liste des employés assignés au projet
    private List<EmployeInfo> employes;
    
    // Classes imbriquées
    @Data
    public static class ClientInfo {
        private Long id;
        private String nom;
        private String prenom;
    }
    
    @Data
    public static class EmployeInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String poste;
    }
}
