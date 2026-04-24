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
}
