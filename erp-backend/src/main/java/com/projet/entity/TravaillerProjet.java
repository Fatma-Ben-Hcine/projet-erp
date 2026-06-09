package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "travailler_projet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"projet", "employe"})
public class TravaillerProjet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(name = "est_chef")
    private Boolean estChef = false;

    public TravaillerProjet(Projet projet, Employe employe) {
        this.projet = projet;
        this.employe = employe;
    }

    public Boolean isEstChef() {
        return estChef;
    }

    public void setEstChef(Boolean estChef) {
        this.estChef = estChef;
    }
}
