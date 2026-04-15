package com.projet.enums;

public enum StatutProjet {
    NOUVEAU("Nouveau"),
    EN_COURS("En cours"),
    TERMINE("Terminé");

    private final String displayName;

    StatutProjet(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
