package com.projet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

@Service
@Slf4j
public class TokenBlacklistService {

    // Utiliser une ConcurrentHashMap pour le thread-safety
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Ajoute un token à la blacklist
     * @param token Le token à blacklister
     */
    public void blacklistToken(String token) {
        // Extraire la date d'expiration du token pour le nettoyer automatiquement
        try {
            // Pour l'instant, on stocke le token avec timestamp
            // Dans une vraie application, on pourrait extraire l'expiration du JWT
            blacklistedTokens.put(token, System.currentTimeMillis());
            log.info("Token ajouté à la blacklist: {}", token.substring(0, Math.min(10, token.length())) + "...");
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du token à la blacklist: {}", e.getMessage());
        }
    }

    /**
     * Vérifie si un token est dans la blacklist
     * @param token Le token à vérifier
     * @return true si le token est blacklisté
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Nettoie les tokens expirés de la blacklist
     * Méthode à appeler périodiquement ou manuellement
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = 24 * 60 * 60 * 1000; // 24 heures en millisecondes
        
        blacklistedTokens.entrySet().removeIf(entry -> {
            boolean isExpired = (currentTime - entry.getValue()) > expirationTime;
            if (isExpired) {
                log.debug("Token expiré retiré de la blacklist: {}", entry.getKey().substring(0, Math.min(10, entry.getKey().length())) + "...");
            }
            return isExpired;
        });
        
        if (!blacklistedTokens.isEmpty()) {
            log.info("Nettoyage de la blacklist effectué. Tokens actifs: {}", blacklistedTokens.size());
        }
    }

    /**
     * Retourne le nombre de tokens dans la blacklist
     * @return Nombre de tokens blacklistés
     */
    public int getBlacklistedTokensCount() {
        return blacklistedTokens.size();
    }
}
