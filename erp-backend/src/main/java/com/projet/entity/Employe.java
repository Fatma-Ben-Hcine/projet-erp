package com.projet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.projet.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;


@Entity
@DiscriminatorValue("EMPLOYE")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Employe extends Utilisateur {


    // Relation avec TravaillerActivite
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employe")
    @Builder.Default
    private List<TravaillerActivite> travaillerActivites = new ArrayList<>();

    // Relation avec TravaillerTache
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employe")
    @Builder.Default
    private List<TravaillerTache> travaillerTaches = new ArrayList<>();

    // Relation avec HeureSupplementaire
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("employe")
    @Builder.Default
    private List<HeureSupplementaire> heuresSupplementaires = new ArrayList<>();

    public Employe() {
        super();
        setRole(Role.ROLE_EMPLOYE);
    }

    public void addTravaillerActivite(TravaillerActivite travaillerActivite) {
        travaillerActivites.add(travaillerActivite);
        travaillerActivite.setEmploye(this);
    }

    public void removeTravaillerActivite(TravaillerActivite travaillerActivite) {
        travaillerActivites.remove(travaillerActivite);
        travaillerActivite.setEmploye(null);
    }

    public void addTravaillerTache(TravaillerTache travaillerTache) {
        travaillerTaches.add(travaillerTache);
        travaillerTache.setEmploye(this);
    }

    public void removeTravaillerTache(TravaillerTache travaillerTache) {
        travaillerTaches.remove(travaillerTache);
        travaillerTache.setEmploye(null);
    }

}
