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
@Table(name = "taches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"travaillerTaches"})
public class Tache {

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

    // Relation ManyToOne avec Activite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;

    // Relation avec TravaillerTache
    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TravaillerTache> travaillerTaches = new HashSet<>();

    public Tache(String nom, String description, LocalDate dateDebut, LocalDate dateFin, Activite activite) {
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.activite = activite;
    }

    public void addTravaillerTache(TravaillerTache travaillerTache) {
        travaillerTaches.add(travaillerTache);
        travaillerTache.setTache(this);
    }

    public void removeTravaillerTache(TravaillerTache travaillerTache) {
        travaillerTaches.remove(travaillerTache);
        travaillerTache.setTache(null);
    }
}
