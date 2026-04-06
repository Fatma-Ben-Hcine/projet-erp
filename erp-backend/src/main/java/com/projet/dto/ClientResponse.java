package com.projet.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClientResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String numeroTelephone;
    private String matriculeFiscale;
    private int nombreContrats;
}
