package com.projet.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ROLE_ADMIN,
    ROLE_EMPLOYE;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }

        switch (value.toUpperCase()) {
            case "ADMIN":
            case "ROLE_ADMIN":
                return ROLE_ADMIN;
            case "EMPLOYE":
            case "ROLE_EMPLOYE":
                return ROLE_EMPLOYE;
            default:
                throw new IllegalArgumentException("Valeur de rôle inconnue: " + value);
        }
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}