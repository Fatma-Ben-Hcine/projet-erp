package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

import com.projet.entity.Client;
import com.projet.entity.TravaillerProjet;
import com.projet.entity.Employe;

@Entity
@Table(name = "projets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_limite", nullable = false)
    private LocalDate dateLimite;

    @Column(nullable = false)
    private Integer progression;

    // Relations avec les employés via TravaillerProjet
    @OneToMany(mappedBy = "projet", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravaillerProjet> travaillerProjets;

    // Chef de projet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_projet_id")
    private Employe chefProjet;

    // Client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
}
