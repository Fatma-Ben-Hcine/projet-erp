package com.projet.controller;

import com.projet.dto.ActiviteRequest;
import com.projet.dto.ActiviteResponse;
import com.projet.enums.StatutActivite;
import com.projet.security.EmployeeProjectSecurityService;
import com.projet.service.ActiviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employe/activites")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeActiviteController {

    private final ActiviteService activiteService;
    private final EmployeeProjectSecurityService securityService;

    // CRUD de base pour les employés
    @GetMapping
    public ResponseEntity<List<ActiviteResponse>> getAllActivites() {
        log.info("GET /api/employe/activites - Récupération de toutes les activités");
        List<ActiviteResponse> activites = activiteService.getAllActivites();
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActiviteResponse> getActiviteById(@PathVariable Long id) {
        log.info("GET /api/employe/activites/{} - Récupération de l'activité", id);
        return activiteService.getActiviteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<ActiviteResponse>> getActivitesByProjetId(@PathVariable Long projetId) {
        log.info("GET /api/employe/activites/projet/{} - Récupération des activités du projet", projetId);
        List<ActiviteResponse> activites = activiteService.getActivitesByProjetId(projetId);
        return ResponseEntity.ok(activites);
    }

    @PostMapping
    public ResponseEntity<?> createActivite(@Valid @RequestBody ActiviteRequest request) {
        log.info("POST /api/employe/activites - Création d'une nouvelle activité: {}", request.getNom());
        try {
            // Check if user is chef de projet for this project
            securityService.checkCurrentUserIsChefDeProjet(request.getProjetId());
            
            ActiviteResponse created = activiteService.createActivite(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la création de l'activité");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivite(
            @PathVariable Long id, 
            @Valid @RequestBody ActiviteRequest request) {
        log.info("PUT /api/employe/activites/{} - Mise à jour de l'activité", id);
        try {
            // Check if user is chef de projet for this project
            securityService.checkCurrentUserIsChefDeProjet(request.getProjetId());
            
            ActiviteResponse updated = activiteService.updateActivite(id, request);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de l'activité: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Gestion des employés
    @PostMapping("/{activiteId}/employes/{employeId}")
    public ResponseEntity<Void> assignEmployeToActivite(
            @PathVariable Long activiteId,
            @PathVariable Long employeId,
            @RequestBody Map<String, Object> requestBody) {
        log.info("POST /api/employe/activites/{}/employes/{} - Assignation d'employé à l'activité", activiteId, employeId);
        
        try {
            Integer progression = 0;
            
            if (requestBody.containsKey("progression")) {
                progression = (Integer) requestBody.get("progression");
            }
            
            activiteService.assignEmployeToActivite(activiteId, employeId, progression);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation de l'employé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{activiteId}/employes/{employeId}")
    public ResponseEntity<Void> unassignEmployeFromActivite(
            @PathVariable Long activiteId,
            @PathVariable Long employeId) {
        log.info("DELETE /api/employe/activites/{}/employes/{} - Désassignation de l'employé de l'activité", activiteId, employeId);
        
        try {
            activiteService.unassignEmployeFromActivite(activiteId, employeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la désassignation de l'employé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{activiteId}/progression")
    public ResponseEntity<Map<String, Object>> getActiviteProgression(@PathVariable Long activiteId) {
        log.info("GET /api/employe/activites/{}/progression - Récupération de la progression de l'activité", activiteId);
        
        try {
            ActiviteResponse activite = activiteService.getActiviteById(activiteId)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
            
            Map<String, Object> response = Map.of(
                "progressionMoyenne", activite.getProgressionMoyenne(),
                "nombreEmployesAssignes", activite.getNombreEmployesAssignes(),
                "nombreEmployesTermines", 0
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la progression: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
