package com.projet.enums;

public enum StatutProjet {
    NOUVEAU("Nouveau"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    EN_RETARD("En retard");

    private final String displayName;

    StatutProjet(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
