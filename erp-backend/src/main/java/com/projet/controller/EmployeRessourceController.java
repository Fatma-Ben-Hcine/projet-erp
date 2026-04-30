package com.projet.controller;

import com.projet.entity.Employe;
import com.projet.entity.Ressource;
import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import com.projet.repository.EmployeRepository;
import com.projet.repository.RessourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employe/ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeRessourceController {

    private final RessourceRepository ressourceRepository;
    private final EmployeRepository employeRepository;

    // Voir toutes les ressources ACTIVES uniquement
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<List<Ressource>> getRessourcesActives(Authentication authentication) {
        log.info("GET /api/employe/ressources - Récupération des ressources actives");
        
        List<Ressource> ressources = ressourceRepository.findByStatut(StatutRessource.ACTIVE);
        log.info("Trouvé {} ressources actives", ressources.size());
        
        return ResponseEntity.ok(ressources);
    }

    // Demander une ressource (cocher)
    @PostMapping("/{id}/demander")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<Map<String, String>> demanderRessource(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("POST /api/employe/ressources/{}/demander - Demande de ressource", id);
        
        String email = authentication.getName();
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + email));
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée: " + id));
        
        // Vérifications
        if (ressource.getStatut() != StatutRessource.ACTIVE) {
            log.warn("Tentative de demander une ressource non active: {}", id);
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cette ressource n'est pas active"));
        }
        
        if (ressource.getSituation() == SituationRessource.DEMANDE) {
            log.warn("Tentative de demander une ressource déjà demandée: {}", id);
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cette ressource est déjà demandée"));
        }
        
        // Marquer comme demandée
        ressource.setSituation(SituationRessource.DEMANDE);
        ressource.setEmployeDemandeur(employe);
        ressource.setDateDemande(java.time.LocalDateTime.now());
        ressourceRepository.save(ressource);
        
        log.info("Ressource {} demandée par l'employé {}", id, employe.getId());
        
        return ResponseEntity.ok(Map.of("message", "Ressource demandée avec succès"));
    }

    // Annuler sa propre demande
    @DeleteMapping("/{id}/annuler")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<Map<String, String>> annulerDemande(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("DELETE /api/employe/ressources/{}/annuler - Annulation de demande", id);
        
        String email = authentication.getName();
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + email));
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée: " + id));
        
        // Vérifier que c'est bien cet employé qui a demandé
        if (ressource.getEmployeDemandeur() == null ||
                !ressource.getEmployeDemandeur().getId().equals(employe.getId())) {
            log.warn("Tentative d'annuler une demande d'un autre employé: ressource={}, employé={}", id, employe.getId());
            return ResponseEntity.status(403)
                    .body(Map.of("erreur", "Vous ne pouvez annuler que vos propres demandes"));
        }
        
        ressource.setSituation(SituationRessource.DISPONIBLE);
        ressource.setEmployeDemandeur(null);
        ressource.setDateDemande(null);
        ressourceRepository.save(ressource);
        
        log.info("Demande de la ressource {} annulée par l'employé {}", id, employe.getId());
        
        return ResponseEntity.ok(Map.of("message", "Demande annulée avec succès"));
    }

    // Voir ses propres demandes
    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<List<Ressource>> getMesDemandes(Authentication authentication) {
        log.info("GET /api/employe/ressources/mes-demandes - Récupération des demandes de l'employé");
        
        String email = authentication.getName();
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + email));
        
        List<Ressource> mesDemandes = ressourceRepository
                .findByEmployeDemandeurIdAndSituation(employe.getId(), SituationRessource.DEMANDE);
        
        log.info("Employé {} a {} demandes en cours", employe.getId(), mesDemandes.size());
        
        return ResponseEntity.ok(mesDemandes);
    }
}
