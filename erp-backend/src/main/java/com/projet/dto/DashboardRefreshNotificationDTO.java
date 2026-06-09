package com.projet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DashboardRefreshNotificationDTO - Notification WebSocket de changement de données
 * Envoyée par le backend aux clients Angular via WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRefreshNotificationDTO {
    
    /**
     * Nom du dataset qui a changé
     */
    private String datasetName;
    
    /**
     * Nouvelle version du dataset
     */
    private String newVersion;
    
    /**
     * Timestamp de la notification
     */
    private LocalDateTime timestamp;
    
    /**
     * Message descriptif (optionnel)
     */
    private String message;
}
