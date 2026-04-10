package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.projet.enums.StatutActivite;

@Entity
@Table(name = "travailler_activite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravaillerActivite {

    @EmbeddedId
    private TravaillerActiviteId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutActivite statut = StatutActivite.EN_COURS;

    @Column(nullable = false)
    private Integer progression = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeId")
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("activiteId")
    @JoinColumn(name = "activite_id")
    private Activite activite;

    @Column(name = "date_debut")
    private java.time.LocalDate dateDebut;

    @Column(name = "date_fin")
    private java.time.LocalDate dateFin;

    public TravaillerActivite(Employe employe, Activite activite) {
        this.employe = employe;
        this.activite = activite;
        this.id = new TravaillerActiviteId(employe.getId(), activite.getId());
        this.dateDebut = java.time.LocalDate.now();
    }

    public TravaillerActivite(Employe employe, Activite activite, StatutActivite statut, Integer progression) {
        this.employe = employe;
        this.activite = activite;
        this.id = new TravaillerActiviteId(employe.getId(), activite.getId());
        this.statut = statut;
        this.progression = progression;
        this.dateDebut = java.time.LocalDate.now();
    }
}
