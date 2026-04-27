package com.projet.dto;

import com.projet.entity.StatutHeureSupplementaire;

import java.time.LocalDate;

public class HeureSupplementaireDTO {
    private Long id;
    private LocalDate date;
    private Double nombreHeures;
    private String mission;
    private StatutHeureSupplementaire statut;
    private Double tarifHeuresSupp;
    private Long employeId;
    private String employeNom;
    private String employePrenom;
    private String employeNomComplet;

    // Constructeurs
    public HeureSupplementaireDTO() {}

    public HeureSupplementaireDTO(LocalDate date, Double nombreHeures, String mission, Double tarifHeuresSupp) {
        this.date = date;
        this.nombreHeures = nombreHeures;
        this.mission = mission;
        this.tarifHeuresSupp = tarifHeuresSupp;
        this.statut = StatutHeureSupplementaire.EN_ATTENTE;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getEmployeNom() {
        return employeNom;
    }

    public void setEmployeNom(String employeNom) {
        this.employeNom = employeNom;
    }

    public String getEmployePrenom() {
        return employePrenom;
    }

    public void setEmployePrenom(String employePrenom) {
        this.employePrenom = employePrenom;
    }

    public String getEmployeNomComplet() {
        return employeNomComplet;
    }

    public void setEmployeNomComplet(String employeNomComplet) {
        this.employeNomComplet = employeNomComplet;
    }
}
