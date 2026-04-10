package com.projet.enums;

public enum StatutActivite {
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    BLOQUE("Bloqué"),
    EN_ATTENTE("En attente"),
    ANNULE("Annulé");

    private final String displayName;

    StatutActivite(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
