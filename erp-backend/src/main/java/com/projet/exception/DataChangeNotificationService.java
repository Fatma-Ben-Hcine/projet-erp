package com.projet.exception;

import com.projet.dto.DashboardRefreshNotificationDTO;
import com.projet.service.DataVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * DataChangeNotificationService
 * Service qui gère les notifications de changement de données
 * 
 * Centralise la logique de notification pour éviter la duplication
 * Appelle DataVersionService pour mettre à jour les versions
 * Notifie les clients via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataChangeNotificationService {

    private final DataVersionService dataVersionService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notifie d'un changement de données
     * À appeler depuis les services métier quand les données changent
     * 
     * Cette méthode:
     * 1. Met à jour la version dans le cache
     * 2. Notifie les clients via WebSocket
     * 
     * @param datasetName nom du dataset qui a changé
     */
    public void notifyDataChange(String datasetName) {
        try {
            log.info("🔔 Notification de changement: {}", datasetName);
            
            // Mettre à jour la version
            var newVersion = dataVersionService.updateDataVersion(datasetName);
            
            // Créer la notification
            DashboardRefreshNotificationDTO notification = DashboardRefreshNotificationDTO.builder()
                    .datasetName(datasetName)
                    .newVersion(newVersion.getVersion())
                    .timestamp(LocalDateTime.now())
                    .message("Les données du " + datasetName + " ont été mises à jour")
                    .build();
            
            // Envoyer aux clients
            messagingTemplate.convertAndSend(
                    "/topic/dashboard/refresh",
                    notification
            );
            
            log.info("✅ Notification envoyée - Version: {}", newVersion.getVersion());
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la notification de changement", e);
            // Ne pas laisser le refresh échouer à cause d'une erreur de notification
        }
    }
}
