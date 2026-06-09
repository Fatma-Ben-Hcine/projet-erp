package com.projet.controller;

import com.projet.dto.TacheRequest;
import com.projet.dto.TacheResponse;
import com.projet.enums.StatutTache;
import com.projet.repository.ActiviteRepository;
import com.projet.security.EmployeeProjectSecurityService;
import com.projet.service.ActiviteService;
import com.projet.service.TacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ActiviteService activiteService;
    private final ActiviteRepository activiteRepository;
    private final EmployeeProjectSecurityService securityService;

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
    public ResponseEntity<?> createTache(@Valid @RequestBody TacheRequest request) {
        log.info("POST /api/employe/taches - Création d'une nouvelle tâche: {}", request.getNom());
        try {
            // Get activity to find project ID
            var activite = activiteService.getActiviteById(request.getActiviteId())
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
            
            // Check if user is chef de projet for this project
            securityService.checkCurrentUserIsChefDeProjet(activite.getProjet().getId());
            
            TacheResponse created = tacheService.createTache(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la création de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la création de la tâche");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTache(
            @PathVariable Long id, 
            @Valid @RequestBody TacheRequest request) {
        log.info("PUT /api/employe/taches/{} - Mise à jour de la tâche", id);
        try {
            // Get activity to find project ID
            var activite = activiteService.getActiviteById(request.getActiviteId())
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
            
            // Check if user is chef de projet for this project
            securityService.checkCurrentUserIsChefDeProjet(activite.getProjet().getId());
            
            TacheResponse updated = tacheService.updateTache(id, request);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la tâche: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Suppression de tâche - uniquement pour le chef de projet
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTache(@PathVariable Long id) {
        log.info("DELETE /api/employe/taches/{} - Suppression de la tâche", id);

        try {
            // Récupérer la tâche pour obtenir l'activitéId
            var tacheResponse = tacheService.getTacheById(id)
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

            // Vérifier que nous avons l'activité
            if (tacheResponse.getActivite() == null) {
                log.error("Impossible de déterminer l'activité pour la tâche {}", id);
                return ResponseEntity.badRequest().body("Impossible de déterminer l'activité pour cette tâche");
            }

            Long activiteId = tacheResponse.getActivite().getId();
            log.info("Activité ID {} trouvée pour la tâche {}", activiteId, id);

            // Récupérer l'activité complète avec le projet (évite le lazy loading)
            var activite = activiteRepository.findByIdWithProjet(activiteId)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
            log.info("Activité récupérée: ID={}, ProjetID={}", activite.getId(), 
                    activite.getProjet() != null ? activite.getProjet().getId() : "null");

            // Vérifier que nous avons le projet
            if (activite.getProjet() == null || activite.getProjet().getId() == null) {
                log.error("Impossible de déterminer le projet pour la tâche {}", id);
                return ResponseEntity.badRequest().body("Impossible de déterminer le projet pour cette tâche");
            }

            Long projetId = activite.getProjet().getId();
            log.info("Vérification chef de projet pour projet {} et tâche {}", projetId, id);

            // Vérifier que l'utilisateur est chef de projet
            securityService.checkCurrentUserIsChefDeProjet(projetId);

            // Supprimer la tâche
            tacheService.deleteTache(id);
            log.info("Tâche {} supprimée avec succès", id);

            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la tâche: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la suppression de la tâche");
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

    // Méthode helper pour la logique commune de dépôt
    private ResponseEntity<?> processDepotTache(Long id, com.projet.dto.DepotRequest request,
                                                 MultipartFile file) {
        System.out.println("=== processDepotTache START ===");
        System.out.println("ID: " + id);
        System.out.println("Request type: " + request.getType());
        System.out.println("Request lien: " + request.getLien());
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));

        try {
            // Récupérer la tâche pour obtenir l'activitéId
            System.out.println("Recherche tâche...");
            var tacheResponse = tacheService.getTacheById(id)
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
            System.out.println("Tâche trouvée: " + tacheResponse.getId());

            // Vérifier que nous avons l'activité
            if (tacheResponse.getActivite() == null) {
                log.error("Impossible de déterminer l'activité pour la tâche {}", id);
                return ResponseEntity.badRequest().body("Impossible de déterminer l'activité pour cette tâche");
            }

            Long activiteId = tacheResponse.getActivite().getId();
            log.info("Activité ID {} trouvée pour la tâche {}", activiteId, id);

            // Récupérer l'activité complète avec le projet
            var activite = activiteRepository.findByIdWithProjet(activiteId)
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée"));

            // Vérifier que nous avons le projet
            if (activite.getProjet() == null || activite.getProjet().getId() == null) {
                log.error("Impossible de déterminer le projet pour la tâche {}", id);
                return ResponseEntity.badRequest().body("Impossible de déterminer le projet pour cette tâche");
            }

            Long projetId = activite.getProjet().getId();
            log.info("Projet ID {} trouvé pour la tâche {}", projetId, id);

            // Check if user is chef de projet
            securityService.checkCurrentUserIsChefDeProjet(projetId);

            // Appeler le service avec le fichier si présent
            System.out.println("Appel tacheService.deposerTache...");
            TacheResponse updated = tacheService.deposerTache(id, request, file);
            System.out.println("=== processDepotTache SUCCESS ===");
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            System.out.println("SecurityException: " + e.getMessage());
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
            log.error("Erreur lors du dépôt de la tâche: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors du dépôt: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            log.error("Erreur inattendue lors du dépôt de la tâche: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Erreur lors du dépôt de la tâche: " + e.getMessage());
        }
    }

    // Dépôt de tâche - UNIQUEMENT @RequestParam, PAS de @RequestBody
    @PostMapping(value = "/{id}/soumettre",
                 consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            MediaType.ALL_VALUE})
    public ResponseEntity<?> soumettreDepotTache(
            @PathVariable Long id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lien", required = false) String lien,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier) {

        System.out.println("=== POST SOUMETTRE TACHE ID: " + id + " ===");
        System.out.println("Type: " + type);
        System.out.println("Lien: " + lien);
        System.out.println("Fichier: " + (fichier != null ? fichier.getOriginalFilename() : "null"));

        // Déterminer le type automatiquement si non fourni
        if (type == null) {
            type = (fichier != null) ? "fichier" : (lien != null ? "lien" : null);
            System.out.println("Type auto-détecté: " + type);
        }

        try {
            com.projet.dto.DepotRequest request = new com.projet.dto.DepotRequest();
            request.setType(type);
            request.setLien(lien);
            request.setNomFichier(fichier != null ? fichier.getOriginalFilename() : null);

            if (request.getType() == null) {
                return ResponseEntity.badRequest().body("Requête invalide: 'type' est requis (lien ou fichier)");
            }

            return processDepotTache(id, request, fichier);

        } catch (Exception e) {
            System.out.println("ERREUR DEPOT: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors du dépôt: " + e.getMessage());
        }
    }

    // Vérifier si une tâche a un dépôt
    @GetMapping("/{id}/depot-exists")
    public ResponseEntity<Map<String, Object>> hasDepot(@PathVariable Long id) {
        log.info("GET /api/employe/taches/{}/depot-exists - Vérification dépôt", id);

        try {
            boolean hasDepot = tacheService.hasDepot(id);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("hasDepot", hasDepot);
            response.put("tacheId", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du dépôt: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
