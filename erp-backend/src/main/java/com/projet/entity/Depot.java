package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "depots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Depot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // 'lien' ou 'fichier'

    @Column(length = 1000)
    private String lien; // URL du dépôt si type = 'lien'

    @Column(length = 500)
    private String nomFichier; // Nom du fichier si type = 'fichier'

    @Column(length = 500)
    private String cheminFichier; // Chemin de stockage du fichier si type = 'fichier'

    @Column(nullable = false)
    private LocalDateTime dateDepot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;
}
