package com.projet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "travailler_tache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"employe", "tache"})
public class TravaillerTache {

    @EmbeddedId
    private TravaillerTacheId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeId")
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tacheId")
    @JoinColumn(name = "tache_id")
    private Tache tache;

    public TravaillerTache(Employe employe, Tache tache) {
        this.employe = employe;
        this.tache = tache;
        this.id = new TravaillerTacheId(employe.getId(), tache.getId());
    }
}
