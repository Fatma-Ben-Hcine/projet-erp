package com.projet.entity;

import com.projet.enums.StatutDemande;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "demande_ressource")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"ressource", "employe"})
public class DemandeRessource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name = "ressource_id", nullable = false)
    @JsonIgnore
    private Ressource ressource;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_demande")
    private StatutDemande statutDemande;
    // EN_ATTENTE, APPROUVEE, ANNULEE
}
