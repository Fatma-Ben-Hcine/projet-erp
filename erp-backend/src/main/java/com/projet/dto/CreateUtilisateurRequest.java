package com.projet.dto;

import com.projet.enums.Role;
import com.projet.enums.TypeUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CreateUtilisateurRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    @NotBlank(message = "Le CIN est obligatoire")
    private String CIN;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String numeroTel;

    @NotBlank(message = "Le poste est obligatoire")
    private String poste;

    private String competences;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    private TypeUtilisateur typeUtilisateur;
    
    private String photo;
}
