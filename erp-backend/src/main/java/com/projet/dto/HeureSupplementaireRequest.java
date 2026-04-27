package com.projet.dto;

import com.projet.entity.StatutHeureSupplementaire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public class HeureSupplementaireRequest {

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le nombre d'heures est obligatoire")
    @Positive(message = "Le nombre d'heures doit être positif")
    @Min(value = 1, message = "Le nombre d'heures minimum est 0.1")
    private Double nombreHeures;

    @NotBlank(message = "La mission est obligatoire")
    private String mission;

    private StatutHeureSupplementaire statut = StatutHeureSupplementaire.EN_ATTENTE;

    @NotNull(message = "Le tarif horaire est obligatoire")
    @Positive(message = "Le tarif horaire doit être positif")
    private Double tarifHeuresSupp;

    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    // Constructeurs
    public HeureSupplementaireRequest() {}

    // Getters et Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getNombreHeures() {
        return nombreHeures;
    }

    public void setNombreHeures(Double nombreHeures) {
        this.nombreHeures = nombreHeures;
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public StatutHeureSupplementaire getStatut() {
        return statut;
    }

    public void setStatut(StatutHeureSupplementaire statut) {
        this.statut = statut;
    }

    public Double getTarifHeuresSupp() {
        return tarifHeuresSupp;
    }

    public void setTarifHeuresSupp(Double tarifHeuresSupp) {
        this.tarifHeuresSupp = tarifHeuresSupp;
    }

    public Long getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }
}
