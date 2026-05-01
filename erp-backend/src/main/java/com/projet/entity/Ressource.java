package com.projet.entity;

import com.projet.enums.StatutRessource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ressources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutRessource statut = StatutRessource.ACTIVE;

    @Column(name = "prix")
    private Double prix;

    @Column(name = "date_debut_abonnement", nullable = true)
    private LocalDate dateDebutAbonnement;

    @Column(name = "date_fin_abonnement", nullable = true)
    private LocalDate dateFinAbonnement;
}
