package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.projet.enums.StatutTache;

@Entity
@Table(name = "travailler_tache")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravaillerTache {

    @EmbeddedId
    private TravaillerTacheId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTache statut = StatutTache.A_FAIRE;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeId")
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tacheId")
    @JoinColumn(name = "tache_id")
    private Tache tache;

    @Column(name = "date_debut")
    private java.time.LocalDate dateDebut;

    @Column(name = "date_fin_reelle")
    private java.time.LocalDate dateFinReelle;

    public TravaillerTache(Employe employe, Tache tache) {
        this.employe = employe;
        this.tache = tache;
        this.id = new TravaillerTacheId(employe.getId(), tache.getId());
        this.dateDebut = java.time.LocalDate.now();
    }

    public TravaillerTache(Employe employe, Tache tache, StatutTache statut) {
        this.employe = employe;
        this.tache = tache;
        this.id = new TravaillerTacheId(employe.getId(), tache.getId());
        this.statut = statut;
        this.dateDebut = java.time.LocalDate.now();
    }
}
