package com.projet.service;

import com.projet.dto.DataVersionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DataVersionService - Gère le versioning des données pour détecter les changements
 * Utilise une combinaison de timestamps et de hashs pour identifier les modifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataVersionService {

    // Cache des versions actuelles (en mémoire)
    // À remplacer par Redis en production
    private final Map<String, DataVersionDTO> versionCache = new HashMap<>();

    /**
     * Récupère la version actuelle des données du dashboard
     */
    public DataVersionDTO getDashboardVersion() {
        String key = "dashboard_main";
        
        return versionCache.computeIfAbsent(key, k -> {
            log.debug("Initialisation de la version dashboard");
            return DataVersionDTO.builder()
                    .datasetName("dashboard_main")
                    .version(generateVersion())
                    .lastUpdated(LocalDateTime.now())
                    .build();
        });
    }

    /**
     * Met à jour la version après un changement de données
     * Appelé par les services métier quand les données changent
     */
    public DataVersionDTO updateDataVersion(String datasetName) {
        log.info("Mise à jour de la version pour: {}", datasetName);
        
        DataVersionDTO currentVersion = versionCache.get(datasetName);
        String newVersion = generateVersion();
        
        DataVersionDTO updatedVersion = DataVersionDTO.builder()
                .datasetName(datasetName)
                .version(newVersion)
                .previousVersion(currentVersion != null ? currentVersion.getVersion() : null)
                .lastUpdated(LocalDateTime.now())
                .build();
        
        versionCache.put(datasetName, updatedVersion);
        return updatedVersion;
    }

    /**
     * Vérifie si les données ont changé
     */
    public boolean hasDataChanged(String datasetName, String clientVersion) {
        DataVersionDTO currentVersion = versionCache.get(datasetName);
        if (currentVersion == null) {
            return true; // Première requête
        }
        
        boolean changed = !currentVersion.getVersion().equals(clientVersion);
        log.debug("Vérification version {} pour {}: {}", datasetName, clientVersion, changed);
        
        return changed;
    }

    /**
     * Génère un nouvel identifiant unique pour la version
     * En production: utiliser timestamp + hash des données critiques
     */
    private String generateVersion() {
        return System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
}
