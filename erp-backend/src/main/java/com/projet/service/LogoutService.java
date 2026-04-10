package com.projet.service;

import com.projet.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public String logout(HttpServletRequest request) {
        // Récupérer le token depuis l'en-tête Authorization
        String token = extractToken(request);
        
        if (token == null) {
            log.warn("Tentative de déconnexion sans token");
            return "Aucun token fourni";
        }

        try {
            // Valider le token
            String email = jwtUtils.extraireEmail(token);
            
            if (email != null && jwtUtils.validerToken(token)) {
                log.info("Déconnexion réussie pour l'utilisateur: {}", email);
                
                // Ajouter le token à la blacklist pour l'invalider
                tokenBlacklistService.blacklistToken(token);
                log.info("Token blacklisté pour l'utilisateur: {}", email);
                
                return "Déconnexion réussie pour l'utilisateur: " + email;
            } else {
                log.warn("Token invalide lors de la déconnexion");
                return "Token invalide";
            }
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            return "Erreur lors de la déconnexion";
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
