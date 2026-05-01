package com.projet.controller;

import com.projet.dto.DemandeRessourceRequest;
import com.projet.entity.Employe;
import com.projet.entity.Ressource;
import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import com.projet.repository.EmployeRepository;
import com.projet.repository.RessourceRepository;
import com.projet.repository.DemandeRessourceRepository;
import com.projet.service.DemandeRessourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employe/ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeRessourceController {

    private final RessourceRepository ressourceRepository;
    private final EmployeRepository employeRepository;
    private final DemandeRessourceRepository demandeRessourceRepository;
    private final DemandeRessourceService demandeService;

    // Voir toutes les ressources ACTIVES avec flag dejaDemandeParMoi
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYE')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRessourcesActives(Authentication authentication) {

        try {
            System.out.println("=== GET /api/employe/ressources ===");

            // 1. Récupérer l'employé connecté
            String email = authentication.getName();
            System.out.println("Email: " + email);

            Employe employe = employeRepository
                .findByEmail(email)
                .orElseThrow(() -> 
                    new RuntimeException("Employé non trouvé: " + email));

            System.out.println("Employé trouvé: " + employe.getId());

            // 2. Récupérer toutes les ressources ACTIVE
            List<Ressource> ressources = ressourceRepository
                .findByStatut(StatutRessource.ACTIVE);

            System.out.println("Ressources trouvées: " + ressources.size());

            // 3. Mapper avec flag dejaDemandeParMoi
            List<Map<String, Object>> response = ressources.stream()
                .map(r -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", r.getId());
                    dto.put("nom", r.getNom());
                    dto.put("description", r.getDescription());
                    dto.put("situation", r.getSituation());
                    dto.put("statut", r.getStatut());
                    dto.put("prix", r.getPrix());
                    dto.put("dateDebut", r.getDateDebut());
                    dto.put("dateFin", r.getDateFin());

                    // Flag : est-ce MOI qui ai demandé ?
                    boolean dejaDemandeParMoi = false;
                    if (r.getSituation() == SituationRessource.DEMANDE
                        && r.getEmployeDemandeur() != null) {
                        dejaDemandeParMoi = r.getEmployeDemandeur()
                            .getId().equals(employe.getId());
                    }
                    dto.put("dejaDemandeParMoi", dejaDemandeParMoi);

                    return dto;
                })
                .collect(Collectors.toList());

            System.out.println("=== SUCCÈS ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== ERREUR: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("erreur", e.getMessage()));
        }
    }

    // Demander une ressource
    @PostMapping("/{id}/demander")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<Map<String, String>> demanderRessource(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("POST /api/employe/ressources/{}/demander - Demande de ressource", id);

        String email = authentication.getName();
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + email));

        DemandeRessourceRequest request = new DemandeRessourceRequest();
        request.setRessourceId(id);

        try {
            demandeService.createDemande(request, employe.getId());
            log.info("Ressource {} demandée par l'employé {}", id, employe.getId());
            return ResponseEntity.ok(Map.of("message", "Ressource demandée avec succès"));
        } catch (RuntimeException e) {
            log.warn("Erreur lors de la demande de ressource {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
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

        try {
            demandeService.annulerDemande(id, employe.getId());
            log.info("Demande de la ressource {} annulée par l'employé {}", id, employe.getId());
            return ResponseEntity.ok(Map.of("message", "Demande annulée avec succès"));
        } catch (RuntimeException e) {
            log.warn("Erreur lors de l'annulation de la demande {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }
}
