package com.projet.controller;

import com.projet.dto.ActiviteRequest;
import com.projet.dto.ActiviteResponse;
import com.projet.enums.StatutActivite;
import com.projet.security.EmployeeProjectSecurityService;
import com.projet.service.ActiviteService;
import com.projet.service.TacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employe/activites")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeActiviteController {

    private final ActiviteService activiteService;
    private final EmployeeProjectSecurityService securityService;
    private final TacheService tacheService;

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

    // Suppression d'activité - uniquement pour le chef de projet
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivite(@PathVariable Long id) {
        log.info("DELETE /api/employe/activites/{} - Suppression de l'activité", id);

        try {
            // Récupérer l'activité pour obtenir le projetId
            var activite = activiteService.getActiviteById(id)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));

            // Vérifier que nous avons les infos nécessaires
            if (activite.getProjet() == null || activite.getProjet().getId() == null) {
                log.error("Impossible de déterminer le projet pour l'activité {}", id);
                return ResponseEntity.badRequest().body("Impossible de déterminer le projet pour cette activité");
            }

            Long projetId = activite.getProjet().getId();
            log.info("Vérification chef de projet pour projet {} et activité {}", projetId, id);

            // Vérifier que l'utilisateur est chef de projet
            securityService.checkCurrentUserIsChefDeProjet(projetId);

            // Supprimer l'activité
            activiteService.deleteActivite(id);
            log.info("Activité {} supprimée avec succès", id);

            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de l'activité: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la suppression de l'activité");
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

    // Récupérer les employés assignés à une activité
    @GetMapping("/{activiteId}/employes")
    public ResponseEntity<List<Map<String, Object>>> getEmployesByActiviteId(@PathVariable Long activiteId) {
        log.info("GET /api/employe/activites/{}/employes - Récupération des employés de l'activité", activiteId);

        try {
            ActiviteResponse activite = activiteService.getActiviteById(activiteId)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));

            List<Map<String, Object>> employes = activite.getEmployeActivites().stream()
                .map(ea -> {
                    Map<String, Object> emp = new HashMap<>();
                    emp.put("id", ea.getEmployeId());
                    emp.put("nom", ea.getEmployeNom());
                    emp.put("prenom", ea.getEmployePrenom());
                    emp.put("progression", ea.getProgression());
                    return emp;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(employes);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des employés: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Dépôt d'activité - POST /soumettre pour éviter les problèmes de PATCH
    @PostMapping(value = "/{id}/soumettre",
                 consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            MediaType.ALL_VALUE})
    public ResponseEntity<?> soumettreDepotActivite(
            @PathVariable Long id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lien", required = false) String lien,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier) {

        System.out.println("=== POST SOUMETTRE ACTIVITE ID: " + id + " ===");
        System.out.println("Type: " + type);
        System.out.println("Lien: " + lien);
        System.out.println("Fichier: " + (fichier != null ? fichier.getOriginalFilename() : "null"));

        // Déterminer le type automatiquement si non fourni
        if (type == null) {
            type = (fichier != null) ? "fichier" : (lien != null ? "lien" : null);
            System.out.println("Type auto-détecté: " + type);
        }

        try {
            // Récupérer l'activité pour obtenir le projetId
            var activite = activiteService.getActiviteById(id)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));

            // Vérifier que nous avons les infos nécessaires
            if (activite.getProjet() == null || activite.getProjet().getId() == null) {
                log.error("Impossible de déterminer le projet pour l'activité {}", id);
                return ResponseEntity.badRequest().body("Impossible de déterminer le projet pour cette activité");
            }

            Long projetId = activite.getProjet().getId();
            log.info("Vérification chef de projet pour projet {} et activité {}", projetId, id);

            // Check if user is chef de projet for this activity's project
            securityService.checkCurrentUserIsChefDeProjet(projetId);

            com.projet.dto.DepotRequest depotRequest = new com.projet.dto.DepotRequest();
            depotRequest.setType(type);
            depotRequest.setLien(lien);
            depotRequest.setNomFichier(fichier != null ? fichier.getOriginalFilename() : null);

            ActiviteResponse updated = activiteService.deposerActivite(id, depotRequest, fichier);
            System.out.println("=== DÉPÔT ACTIVITE RÉUSSI ===");
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors du dépôt de l'activité: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors du dépôt de l'activité: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors du dépôt de l'activité");
        }
    }

    // Vérifier si une activité a un dépôt
    @GetMapping("/{id}/depot-exists")
    public ResponseEntity<Map<String, Object>> hasDepot(@PathVariable Long id) {
        log.info("GET /api/employe/activites/{}/depot-exists - Vérification dépôt", id);

        try {
            boolean hasDepot = activiteService.hasDepot(id);
            Map<String, Object> response = new HashMap<>();
            response.put("hasDepot", hasDepot);
            response.put("activiteId", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du dépôt: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Vérifier si toutes les tâches d'une activité sont déposées
    @GetMapping("/{id}/toutes-taches-deposees")
    public ResponseEntity<Map<String, Object>> areAllTachesDeposees(@PathVariable Long id) {
        log.info("GET /api/employe/activites/{}/toutes-taches-deposees", id);

        try {
            var activite = activiteService.getActiviteById(id)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));

            boolean allDeposees = true;
            if (activite.getTaches() != null && !activite.getTaches().isEmpty()) {
                for (var tache : activite.getTaches()) {
                    if (!tacheService.hasDepot(tache.getId())) {
                        allDeposees = false;
                        break;
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("allDeposees", allDeposees);
            response.put("activiteId", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des tâches: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
