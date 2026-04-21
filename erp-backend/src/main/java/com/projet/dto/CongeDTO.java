package com.projet.dto;

import com.projet.entity.TypeConge;
import com.projet.entity.StatutConge;

import java.time.LocalDate;

public class CongeDTO {
    private Long id;
    private TypeConge typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutConge statut;
    private Long employeId;
    private String employeNom;
    private String employePrenom;
    private String employeNomComplet;

    // Constructeurs
    public CongeDTO() {}

    public CongeDTO(TypeConge typeConge, LocalDate dateDebut, LocalDate dateFin) {
        this.typeConge = typeConge;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = StatutConge.EN_ATTENTE;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeConge getTypeConge() {
        return typeConge;
    }

    public void setTypeConge(TypeConge typeConge) {
        this.typeConge = typeConge;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutConge getStatut() {
        return statut;
    }

    public void setStatut(StatutConge statut) {
        this.statut = statut;
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
