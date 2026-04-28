package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "activites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"taches", "travaillerActivites"})
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

    @Column(name = "est_depose", nullable = false)
    private boolean estDeposé = false;

    // Relation ManyToOne avec Projet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    // Relation OneToMany avec Tache
    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tache> taches = new HashSet<>();

    // Relation avec TravaillerActivite
    @OneToMany(mappedBy = "activite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TravaillerActivite> travaillerActivites = new HashSet<>();

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
