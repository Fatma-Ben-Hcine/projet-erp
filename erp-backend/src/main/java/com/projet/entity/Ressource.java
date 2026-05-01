package com.projet.entity;

import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ressources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"employeDemandeur"})
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
    // ACTIVE, NON_ACTIVE

    @Enumerated(EnumType.STRING)
    @Column(name = "situation")
    private SituationRessource situation = SituationRessource.NON_DEMANDE;
    // NON_DEMANDE, DEMANDE

    @Column(name = "prix")
    private Double prix;

    // Anciens champs conservés pour compatibilité (à supprimer plus tard)
    @Column(name = "date_debut", nullable = true)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = true)
    private LocalDate dateFin;

    // Pour savoir qui a demandé actuellement
    // Non visible dans le diagramme mais nécessaire
    // pour la logique situation DEMANDE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_demandeur_id", nullable = true)
    @JsonIgnore
    private Employe employeDemandeur;

    @Column(name = "date_demande", nullable = true)
    private LocalDateTime dateDemande;
}
