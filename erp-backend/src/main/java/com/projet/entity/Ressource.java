package com.projet.entity;

import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ressources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"demandes", "projets"})
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituationRessource situation = SituationRessource.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRessource statut = StatutRessource.ACTIVE;

    // Employé qui a demandé la ressource (nullable)
    @ManyToOne
    @JoinColumn(name = "employe_demandeur_id", nullable = true)
    private Employe employeDemandeur;

    @Column(name = "date_demande", nullable = true)
    private LocalDateTime dateDemande;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    // Relation avec les demandes de ressources
    @OneToMany(mappedBy = "ressource", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandeRessource> demandes = new HashSet<>();

    // Relation avec les projets (0..* - 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    private Projet projet;
}
