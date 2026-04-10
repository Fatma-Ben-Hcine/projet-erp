package com.projet.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String nom;
    private String prenom;
    private String email;
    private String CIN;
    private String numeroTel;
    private String poste;
    private String competences;
    private String role;
    private String photo;
}
