package com.projet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatutSchedulerService {

    private final RessourceService ressourceService;

    /**
     * Cron job exécuté chaque jour à 1h du matin pour mettre à jour 
     * automatiquement les statuts des ressources dont l'abonnement a expiré.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void updateStatutsExpires() {
        log.info("=== DÉBUT DU CRON JOB - MISE À JOUR AUTOMATIQUE DES STATUTS ===");
        
        try {
            ressourceService.updateStatutsByDate();
            log.info("=== CRON JOB TERMINÉ AVEC SUCCÈS ===");
        } catch (Exception e) {
            log.error("=== ERREUR LORS DU CRON JOB DE MISE À JOUR DES STATUTS ===", e);
        }
    }
}
