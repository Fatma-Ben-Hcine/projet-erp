package com.projet.entity;

import com.projet.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Admin extends Utilisateur {

    public Admin() {
        super();
        setRole(Role.ROLE_ADMIN);
    }
}
