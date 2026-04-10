package com.projet.controller;

import com.projet.dto.ActiviteRequest;
import com.projet.dto.ActiviteResponse;
import com.projet.enums.StatutActivite;
import com.projet.service.ActiviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activites")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActiviteController {

    private final ActiviteService activiteService;

    // CRUD de base
    @GetMapping
    public ResponseEntity<List<ActiviteResponse>> getAllActivites() {
        log.info("GET /api/admin/activites - Récupération de toutes les activités");
        List<ActiviteResponse> activites = activiteService.getAllActivites();
        return ResponseEntity.ok(activites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActiviteResponse> getActiviteById(@PathVariable Long id) {
        log.info("GET /api/admin/activites/{} - Récupération de l'activité", id);
        return activiteService.getActiviteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<ActiviteResponse>> getActivitesByProjetId(@PathVariable Long projetId) {
        log.info("GET /api/admin/activites/projet/{} - Récupération des activités du projet", projetId);
        List<ActiviteResponse> activites = activiteService.getActivitesByProjetId(projetId);
        return ResponseEntity.ok(activites);
    }

    @PostMapping
    public ResponseEntity<ActiviteResponse> createActivite(@Valid @RequestBody ActiviteRequest request) {
        log.info("POST /api/admin/activites - Création d'une nouvelle activité: {}", request.getNom());
        try {
            ActiviteResponse created = activiteService.createActivite(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActiviteResponse> updateActivite(
            @PathVariable Long id, 
            @Valid @RequestBody ActiviteRequest request) {
        log.info("PUT /api/admin/activites/{} - Mise à jour de l'activité", id);
        try {
            ActiviteResponse updated = activiteService.updateActivite(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de l'activité: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivite(@PathVariable Long id) {
        log.info("DELETE /api/admin/activites/{} - Suppression de l'activité", id);
        try {
            activiteService.deleteActivite(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de l'activité: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Gestion des employés
    @PostMapping("/{activiteId}/employes/{employeId}")
    public ResponseEntity<Void> assignEmployeToActivite(
            @PathVariable Long activiteId,
            @PathVariable Long employeId,
            @RequestBody Map<String, Object> requestBody) {
        log.info("POST /api/admin/activites/{}/employes/{} - Assignation d'employé à l'activité", activiteId, employeId);
        
        try {
            StatutActivite statut = StatutActivite.EN_COURS;
            Integer progression = 0;
            
            if (requestBody.containsKey("statut")) {
                statut = StatutActivite.valueOf((String) requestBody.get("statut"));
            }
            if (requestBody.containsKey("progression")) {
                progression = (Integer) requestBody.get("progression");
            }
            
            activiteService.assignEmployeToActivite(activiteId, employeId, statut, progression);
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
        log.info("DELETE /api/admin/activites/{}/employes/{} - Désassignation de l'employé de l'activité", activiteId, employeId);
        
        try {
            activiteService.unassignEmployeFromActivite(activiteId, employeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la désassignation de l'employé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{activiteId}/employes/{employeId}/progression")
    public ResponseEntity<Void> updateEmployeActiviteProgression(
            @PathVariable Long activiteId,
            @PathVariable Long employeId,
            @RequestBody Map<String, Integer> requestBody) {
        log.info("PUT /api/admin/activites/{}/employes/{}/progression - Mise à jour de la progression", activiteId, employeId);
        
        try {
            Integer progression = requestBody.get("progression");
            if (progression == null || progression < 0 || progression > 100) {
                return ResponseEntity.badRequest().build();
            }
            
            activiteService.updateEmployeActiviteProgression(activiteId, employeId, progression);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la progression: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{activiteId}/progression")
    public ResponseEntity<Map<String, Object>> getActiviteProgression(@PathVariable Long activiteId) {
        log.info("GET /api/admin/activites/{}/progression - Récupération de la progression de l'activité", activiteId);
        
        try {
            ActiviteResponse activite = activiteService.getActiviteById(activiteId)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
            
            Map<String, Object> response = Map.of(
                "progressionMoyenne", activite.getProgressionMoyenne(),
                "nombreEmployesAssignes", activite.getNombreEmployesAssignes(),
                "nombreEmployesTermines", activite.getEmployeActivites().stream()
                    .mapToInt(emp -> emp.getStatut() == StatutActivite.TERMINE ? 1 : 0)
                    .sum()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la progression: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
