package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "travailler_activite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"employe", "activite"})
public class TravaillerActivite {

    @EmbeddedId
    private TravaillerActiviteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeId")
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("activiteId")
    @JoinColumn(name = "activite_id")
    private Activite activite;

    public TravaillerActivite(Employe employe, Activite activite) {
        this.employe = employe;
        this.activite = activite;
        this.id = new TravaillerActiviteId(employe.getId(), activite.getId());
    }
}
