package com.projet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_id")
    @JsonIgnore
    private Employe destinataire;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type;
    // "PROJET_ASSIGNE" / "DATE_LIMITE_PROCHE"

    @Column(name = "projet_id")
    private Long projetId;

    @Column(name = "projet_nom")
    private String projetNom;

    @Column(name = "est_lue")
    private boolean estLue = false;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
}
