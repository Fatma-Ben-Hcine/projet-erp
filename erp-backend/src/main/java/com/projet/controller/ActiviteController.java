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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<?> createActivite(@Valid @RequestBody ActiviteRequest request) {
        log.info("POST /api/admin/activites - Création d'une nouvelle activité: {}", request.getNom());
        try {
            ActiviteResponse created = activiteService.createActivite(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            log.error("Erreur de validation lors de la création de l'activité: {}", e.getReason());
            return ResponseEntity.badRequest().body(e.getReason());
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivite(
            @PathVariable Long id, 
            @Valid @RequestBody ActiviteRequest request) {
        log.info("PUT /api/admin/activites/{} - Mise à jour de l'activité", id);
        try {
            ActiviteResponse updated = activiteService.updateActivite(id, request);
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException e) {
            log.error("Erreur de validation lors de la mise à jour de l'activité: {}", e.getReason());
            return ResponseEntity.badRequest().body(e.getReason());
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de l'activité: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
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
            @PathVariable Long employeId) {
        log.info("POST /api/admin/activites/{}/employes/{} - Assignation d'employé à l'activité", activiteId, employeId);
        
        try {
            activiteService.assignEmployeToActivite(activiteId, employeId);
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

    // Endpoint supprimé : la progression est calculée dynamiquement
    // @PutMapping("/{activiteId}/employes/{employeId}/progression")
    // public ResponseEntity<Void> updateEmployeActiviteProgression(...) { ... }

    @GetMapping("/{activiteId}/progression")
    public ResponseEntity<Map<String, Object>> getActiviteProgression(@PathVariable Long activiteId) {
        log.info("GET /api/admin/activites/{}/progression - Récupération de la progression de l'activité", activiteId);

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
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{activiteId}/employes")
    public ResponseEntity<List<Map<String, Object>>> getEmployesByActiviteId(@PathVariable Long activiteId) {
        log.info("GET /api/admin/activites/{}/employes - Récupération des employés de l'activité", activiteId);
        try {
            List<Map<String, Object>> employes = activiteService.getEmployesByActiviteId(activiteId);
            return ResponseEntity.ok(employes);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des employés: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/depot-exists")
    public ResponseEntity<Map<String, Object>> checkDepotExists(@PathVariable Long id) {
        log.info("GET /api/admin/activites/{}/depot-exists - Vérification dépôt activité", id);
        try {
            boolean exists = activiteService.hasDepot(id);
            Map<String, Object> response = Map.of(
                "hasDepot", exists,
                "activiteId", id
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du dépôt: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/toutes-taches-deposees")
    public ResponseEntity<Map<String, Object>> checkAllTachesDeposees(@PathVariable Long id) {
        log.info("GET /api/admin/activites/{}/toutes-taches-deposees - Vérification tâches déposées", id);
        try {
            boolean allDeposees = activiteService.areAllTachesDeposees(id);
            Map<String, Object> response = Map.of(
                "allDeposees", allDeposees,
                "activiteId", id
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des tâches: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/{id}/depot", consumes = "multipart/form-data")
    public ResponseEntity<?> deposerActivite(
            @PathVariable Long id,
            @RequestPart("type") String type,
            @RequestPart(value = "lien", required = false) String lien,
            @RequestPart(value = "nomFichier", required = false) String nomFichier,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        log.info("PATCH /api/admin/activites/{}/depot - Dépôt de l'activité", id);
        try {
            com.projet.dto.DepotRequest depotRequest = new com.projet.dto.DepotRequest();
            depotRequest.setType(type);
            depotRequest.setLien(lien);
            depotRequest.setNomFichier(nomFichier);
            depotRequest.setCheminFichier(null);

            ActiviteResponse response = activiteService.deposerActivite(id, depotRequest, file);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors du dépôt de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du dépôt de l'activité: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors du dépôt de l'activité");
        }
    }
}
