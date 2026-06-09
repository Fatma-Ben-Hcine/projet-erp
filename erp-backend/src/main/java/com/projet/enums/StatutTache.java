package com.projet.enums;

public enum StatutTache {
    A_FAIRE("À faire"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    BLOQUE("Bloqué"),
    EN_REVISION("En révision");

    private final String displayName;

    StatutTache(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
