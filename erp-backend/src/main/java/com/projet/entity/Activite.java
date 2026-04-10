package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "activites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    // Relation ManyToOne avec Projet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    // Relation OneToMany avec Tache
    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> taches = new ArrayList<>();

    // Relation avec TravaillerActivite
    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravaillerActivite> travaillerActivites = new ArrayList<>();

    public Activite(String nom, String description, LocalDate dateDebut, LocalDate dateFin, Projet projet) {
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.projet = projet;
    }

    public void addTache(Tache tache) {
        taches.add(tache);
        tache.setActivite(this);
    }

    public void removeTache(Tache tache) {
        taches.remove(tache);
        tache.setActivite(null);
    }

    public void addTravaillerActivite(TravaillerActivite travaillerActivite) {
        travaillerActivites.add(travaillerActivite);
        travaillerActivite.setActivite(this);
    }

    public void removeTravaillerActivite(TravaillerActivite travaillerActivite) {
        travaillerActivites.remove(travaillerActivite);
        travaillerActivite.setActivite(null);
    }
}
