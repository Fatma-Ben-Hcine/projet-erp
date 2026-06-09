package com.projet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DataVersionDTO - Représente la version actuelle des données
 * Utilisé pour le polling intelligent et la détection de changements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataVersionDTO {
    
    /**
     * Identifiant du dataset (ex: "dashboard_main", "projects", "resources", etc.)
     */
    private String datasetName;
    
    /**
     * Version unique des données (timestamp + hash)
     */
    private String version;
    
    /**
     * Version précédente (utile pour audit)
     */
    private String previousVersion;
    
    /**
     * Date de dernière mise à jour
     */
    private LocalDateTime lastUpdated;
    
    /**
     * Indique si les données ont changé depuis la requête du client
     * (rempli uniquement si clientVersion fourni)
     */
    @Builder.Default
    private Boolean hasChanged = false;
}
