package com.projet.entity;

import com.projet.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@DiscriminatorValue("CHEF_PROJET")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ChefProjet extends Employe {

    public ChefProjet() {
        super();
        setRole(Role.ROLE_CHEF_PROJET);
    }

    @OneToMany(mappedBy = "chefProjet", fetch = FetchType.LAZY)
    private List<Projet> projetsDiriges;
}
