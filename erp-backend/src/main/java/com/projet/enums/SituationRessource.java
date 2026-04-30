package com.projet.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum SituationRessource {
    @JsonProperty("disponible")
    DISPONIBLE("disponible"),
    @JsonProperty("demandé")
    DEMANDE("demandé");

    private final String displayName;

    SituationRessource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SituationRessource fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        return switch (value.toLowerCase().replace(" ", "_")) {
            case "disponible" -> DISPONIBLE;
            case "demande", "demandé" -> DEMANDE;
            default -> throw new IllegalArgumentException("Situation inconnue: " + value);
        };
    }
}
