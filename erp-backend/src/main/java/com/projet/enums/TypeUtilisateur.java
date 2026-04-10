package com.projet.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeUtilisateur {
    PERMANENT,
    TEMPORAIRE;

    @JsonCreator
    public static TypeUtilisateur fromString(String value) {
        if (value == null) {
            return null;
        }
        
        switch (value.toUpperCase()) {
            case "PERMANENT":
            case "PERMANANT":
                return PERMANENT;
            case "TEMPORAIRE":
            case "TEMPORARY":
                return TEMPORAIRE;
            default:
                throw new IllegalArgumentException("Valeur de typeUtilisateur inconnue: " + value);
        }
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}
