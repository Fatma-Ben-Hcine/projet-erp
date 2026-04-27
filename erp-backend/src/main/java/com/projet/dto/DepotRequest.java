package com.projet.dto;

import lombok.Data;

@Data
public class DepotRequest {
    private String type; // 'lien' ou 'fichier'
    private String lien; // URL du dépôt si type = 'lien'
    private String nomFichier; // Nom du fichier si type = 'fichier'
    private String cheminFichier; // Chemin de stockage du fichier si type = 'fichier'
}
