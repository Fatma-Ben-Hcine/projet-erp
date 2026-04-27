package com.projet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepotResponse {
    private Long id;
    private String type;
    private String lien;
    private String nomFichier;
    private String cheminFichier;
    @JsonProperty("dateDepot")
    private LocalDateTime dateDepot;
    
    // IDs des entités liées pour distinguer les niveaux hiérarchiques
    @JsonProperty("tacheId")
    private Long tacheId;
    @JsonProperty("activiteId")
    private Long activiteId;
    @JsonProperty("projetId")
    private Long projetId;
}
