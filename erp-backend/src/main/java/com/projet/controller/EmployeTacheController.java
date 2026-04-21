package com.projet.controller;

import com.projet.dto.TacheRequest;
import com.projet.dto.TacheResponse;
import com.projet.enums.StatutTache;
import com.projet.service.TacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employe/taches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeTacheController {

    private final TacheService tacheService;

    // CRUD de base pour les employés
    @GetMapping
    public ResponseEntity<List<TacheResponse>> getAllTaches() {
        log.info("GET /api/employe/taches - Récupération de toutes les tâches");
        List<TacheResponse> taches = tacheService.getAllTaches();
        return ResponseEntity.ok(taches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TacheResponse> getTacheById(@PathVariable Long id) {
        log.info("GET /api/employe/taches/{} - Récupération de la tâche", id);
        return tacheService.getTacheById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/activite/{activiteId}")
    public ResponseEntity<List<TacheResponse>> getTachesByActiviteId(@PathVariable Long activiteId) {
        log.info("GET /api/employe/taches/activite/{} - Récupération des tâches de l'activité", activiteId);
        List<TacheResponse> taches = tacheService.getTachesByActiviteId(activiteId);
        return ResponseEntity.ok(taches);
    }

    @PostMapping
    public ResponseEntity<TacheResponse> createTache(@Valid @RequestBody TacheRequest request) {
        log.info("POST /api/employe/taches - Création d'une nouvelle tâche: {}", request.getNom());
        try {
            TacheResponse created = tacheService.createTache(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TacheResponse> updateTache(
            @PathVariable Long id, 
            @Valid @RequestBody TacheRequest request) {
        log.info("PUT /api/employe/taches/{} - Mise à jour de la tâche", id);
        try {
            TacheResponse updated = tacheService.updateTache(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la tâche: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        log.info("DELETE /api/employe/taches/{} - Suppression de la tâche", id);
        try {
            tacheService.deleteTache(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la tâche: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Gestion des employés
    @PostMapping("/{tacheId}/employes/{employeId}")
    public ResponseEntity<Void> assignEmployeToTache(
            @PathVariable Long tacheId,
            @PathVariable Long employeId,
            @RequestBody Map<String, Object> requestBody) {
        log.info("POST /api/employe/taches/{}/employes/{} - Assignation d'employé à la tâche", tacheId, employeId);
        
        try {
            tacheService.assignEmployeToTache(tacheId, employeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation de l'employé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{tacheId}/employes/{employeId}")
    public ResponseEntity<Void> unassignEmployeFromTache(
            @PathVariable Long tacheId,
            @PathVariable Long employeId) {
        log.info("DELETE /api/employe/taches/{}/employes/{} - Désassignation de l'employé de la tâche", tacheId, employeId);
        
        try {
            tacheService.unassignEmployeFromTache(tacheId, employeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la désassignation de l'employé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
