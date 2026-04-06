package com.projet.entity;

import com.projet.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("EMPLOYE")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Employe extends Utilisateur {

    public Employe() {
        super();
        setRole(Role.ROLE_EMPLOYE);
    }
}
