package com.projet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "heures_supplementaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeureSupplementaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double nombreHeures;

    @Column(nullable = false)
    private String mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutHeureSupplementaire statut = StatutHeureSupplementaire.EN_ATTENTE;

    @Column(name = "tarif_heures_supp", nullable = false)
    private Double tarifHeuresSupp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_id", nullable = false)
    @JsonIgnoreProperties("heuresSupplementaires")
    private Employe employe;
}
