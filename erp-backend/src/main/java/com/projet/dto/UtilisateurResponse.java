package com.projet.dto;

import com.projet.enums.TypeUtilisateur;
import lombok.Data;

@Data
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String CIN;
    private String numeroTel;
    private String poste;
    private String competences;
    private String role;
    private TypeUtilisateur typeUtilisateur;
    private boolean actif;
    private String photo;
}
