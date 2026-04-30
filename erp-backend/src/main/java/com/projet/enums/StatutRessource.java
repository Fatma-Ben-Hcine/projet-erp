package com.projet.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum StatutRessource {
    @JsonProperty("active")
    ACTIVE("active"),
    @JsonProperty("non active")
    NON_ACTIVE("non active");

    private final String displayName;

    StatutRessource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static StatutRessource fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        return switch (value.toLowerCase().replace(" ", "_")) {
            case "active" -> ACTIVE;
            case "non_active", "non active" -> NON_ACTIVE;
            default -> throw new IllegalArgumentException("Statut inconnu: " + value);
        };
    }
}
