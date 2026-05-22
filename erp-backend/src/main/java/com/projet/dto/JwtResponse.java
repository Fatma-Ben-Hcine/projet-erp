package com.projet.dto;

import lombok.Data;

@Data
public class JwtResponse {

    private String token;
    private String type;
    private String email;
    private String role;
    private Long id;
    private String nom;
    private String prenom;

    public JwtResponse(String token, String type, String email, String role, Long id) {
        this.token = token;
        this.type = type;
        this.email = email;
        this.role = role;
        this.id = id;
    }

    public JwtResponse(String token, String type, String email, String role, Long id, String nom, String prenom) {
        this.token = token;
        this.type = type;
        this.email = email;
        this.role = role;
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }
}
