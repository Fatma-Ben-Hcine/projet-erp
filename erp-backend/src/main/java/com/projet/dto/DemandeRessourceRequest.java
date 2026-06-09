package com.projet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRessourceRequest {

    @NotNull(message = "L'ID de la ressource est obligatoire")
    private Long ressourceId;
}
