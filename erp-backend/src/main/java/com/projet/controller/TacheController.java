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
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/taches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class TacheController {

    private final TacheService tacheService;

    // CRUD de base
    @GetMapping
    public ResponseEntity<List<TacheResponse>> getAllTaches() {
        log.info("GET /api/admin/taches - Récupération de toutes les tâches");
        List<TacheResponse> taches = tacheService.getAllTaches();
        return ResponseEntity.ok(taches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TacheResponse> getTacheById(@PathVariable Long id) {
        log.info("GET /api/admin/taches/{} - Récupération de la tâche", id);
        return tacheService.getTacheById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/activite/{activiteId}")
    public ResponseEntity<List<TacheResponse>> getTachesByActiviteId(@PathVariable Long activiteId) {
        log.info("GET /api/admin/taches/activite/{} - Récupération des tâches de l'activité", activiteId);
        List<TacheResponse> taches = tacheService.getTachesByActiviteId(activiteId);
        return ResponseEntity.ok(taches);
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<TacheResponse>> getTachesByEmployeId(@PathVariable Long employeId) {
        log.info("GET /api/admin/taches/employe/{} - Récupération des tâches de l'employé", employeId);
        List<TacheResponse> taches = tacheService.getTachesByEmployeId(employeId);
        return ResponseEntity.ok(taches);
    }

    @PostMapping
    public ResponseEntity<TacheResponse> createTache(@Valid @RequestBody TacheRequest request) {
        log.info("POST /api/admin/taches - Création d'une nouvelle tâche: {}", request.getNom());
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
        log.info("PUT /api/admin/taches/{} - Mise à jour de la tâche", id);
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
        log.info("DELETE /api/admin/taches/{} - Suppression de la tâche", id);
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
            @RequestBody Map<String, String> requestBody) {
        log.info("POST /api/admin/taches/{}/employes/{} - Assignation d'employé à la tâche", tacheId, employeId);
        
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
        log.info("DELETE /api/admin/taches/{}/employes/{} - Désassignation de l'employé de la tâche", tacheId, employeId);
        
        try {
            tacheService.unassignEmployeFromTache(tacheId, employeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la désassignation de l'employé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    
    // Statistiques
    @GetMapping("/activite/{activiteId}/count")
    public ResponseEntity<Map<String, Long>> getTachesCountByActivite(@PathVariable Long activiteId) {
        log.info("GET /api/admin/taches/activite/{}/count - Nombre de tâches pour l'activité", activiteId);
        
        try {
            long count = tacheService.getNombreTachesByActivite(activiteId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Erreur lors du comptage des tâches: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/employe/{employeId}/terminees/count")
    public ResponseEntity<Map<String, Long>> getTachesTermineesByEmploye(@PathVariable Long employeId) {
        log.info("GET /api/admin/taches/employe/{}/terminees/count - Tâches terminées pour l'employé", employeId);
        
        try {
            long count = tacheService.getNombreTachesTermineesByEmploye(employeId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Erreur lors du comptage des tâches terminées: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{tacheId}/terminees/count")
    public ResponseEntity<Map<String, Long>> getTachesTermineesByTache(@PathVariable Long tacheId) {
        log.info("GET /api/admin/taches/{}/terminees/count - Employés ayant terminé la tâche", tacheId);
        
        try {
            long count = tacheService.getNombreTachesTermineesByTache(tacheId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Erreur lors du comptage des employés ayant terminé: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{tacheId}/progression")
    public ResponseEntity<Map<String, Object>> getTacheProgression(@PathVariable Long tacheId) {
        log.info("GET /api/admin/taches/{}/progression - Récupération de la progression de la tâche", tacheId);

        try {
            TacheResponse tache = tacheService.getTacheById(tacheId)
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

            Map<String, Object> response = Map.of(
                "progression", tache.getProgression(),
                "nombreEmployesAssignes", tache.getNombreEmployesAssignes(),
                "nombreEmployesTermines", tache.getNombreEmployesTermines()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la progression: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping(value = "/{id}/depot", consumes = "multipart/form-data")
    public ResponseEntity<?> deposerTache(
            @PathVariable Long id,
            @RequestPart("type") String type,
            @RequestPart(value = "lien", required = false) String lien,
            @RequestPart(value = "nomFichier", required = false) String nomFichier,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info("PATCH /api/admin/taches/{}/depot - Dépôt de la tâche", id);
        try {
            com.projet.dto.DepotRequest depotRequest = new com.projet.dto.DepotRequest();
            depotRequest.setType(type);
            depotRequest.setLien(lien);
            depotRequest.setNomFichier(nomFichier);
            depotRequest.setCheminFichier(null);

            TacheResponse response = tacheService.deposerTache(id, depotRequest, file);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors du dépôt de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du dépôt de la tâche: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors du dépôt de la tâche");
        }
    }
}
