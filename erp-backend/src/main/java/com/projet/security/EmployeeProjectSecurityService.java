package com.projet.security;

import com.projet.entity.Employe;
import com.projet.entity.TravaillerProjet;
import com.projet.entity.Utilisateur;
import com.projet.repository.TravaillerProjetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeProjectSecurityService {

    private final TravaillerProjetRepository travaillerProjetRepository;

    /**
     * Get the current authenticated user ID
     * @return the user ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Utilisateur) {
            return ((Utilisateur) principal).getId();
        }
        
        return null;
    }

    /**
     * Check if the current user is chef de projet for a specific project
     * @param projetId the project ID
     * @return true if the current user is chef de projet, false otherwise
     */
    public boolean isCurrentUserChefDeProjet(Long projetId) {
        Long currentUserId = getCurrentUserId();

        log.info("[DEBUG] isCurrentUserChefDeProjet called - projetId: {}, currentUserId: {}", projetId, currentUserId);

        if (currentUserId == null) {
            log.warn("No authenticated user found when checking chef de projet for project {}", projetId);
            return false;
        }

        // Vérifier via la table travailler_projet avec le champ est_chef
        Optional<TravaillerProjet> tp = travaillerProjetRepository
                .findByEmployeIdAndProjetId(currentUserId, projetId);

        boolean isChef = tp.isPresent() && Boolean.TRUE.equals(tp.get().getEstChef());

        // Logs de debug détaillés
        log.info("[DEBUG] Chef check details - User: {}, Project: {}, TravaillerProjet present: {}, estChef value: {}, isChef result: {}",
                currentUserId, projetId, tp.isPresent(),
                tp.isPresent() ? tp.get().getEstChef() : "N/A",
                isChef);

        return isChef;
    }

    /**
     * Check if the current user is chef de projet for a specific project
     * This method throws an exception if not authorized
     * @param projetId the project ID
     * @throws SecurityException if the current user is not chef de projet
     */
    public void checkCurrentUserIsChefDeProjet(Long projetId) {
        if (!isCurrentUserChefDeProjet(projetId)) {
            Long currentUserId = getCurrentUserId();
            log.warn("User {} attempted unauthorized action on project {}", currentUserId, projetId);
            throw new SecurityException("Vous n'êtes pas autorisé à effectuer cette action. Seul le chef de projet peut modifier ce projet.");
        }
    }

}
