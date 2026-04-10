package com.projet.dto;

import com.projet.enums.TypeUtilisateur;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUtilisateurRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le CIN est obligatoire")
    private String CIN;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String numeroTel;

    @NotBlank(message = "Le poste est obligatoire")
    private String poste;

    private String competences;
    
    private TypeUtilisateur typeUtilisateur;
    
    private String photo;
}
