package com.projet.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravaillerActiviteId implements Serializable {

    private Long employeId;
    private Long activiteId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravaillerActiviteId that = (TravaillerActiviteId) o;
        return Objects.equals(employeId, that.employeId) && 
               Objects.equals(activiteId, that.activiteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeId, activiteId);
    }
}
