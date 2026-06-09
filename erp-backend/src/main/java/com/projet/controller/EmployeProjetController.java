package com.projet.controller;

import com.projet.dto.ProjetResponse;
import com.projet.entity.Projet;
import com.projet.entity.TravaillerProjet;
import com.projet.entity.Utilisateur;
import com.projet.repository.ProjetRepository;
import com.projet.repository.TravaillerProjetRepository;
import com.projet.security.EmployeeProjectSecurityService;
import com.projet.service.ProjetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employe/projets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeProjetController {

    private final ProjetService projetService;
    private final ProjetRepository projetRepository;
    private final TravaillerProjetRepository travaillerProjetRepository;
    private final EmployeeProjectSecurityService securityService;

    /**
     * Get current authenticated user ID from security context
     */
    private Long getCurrentUserId() {
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
     * Get all projects assigned to the current employee
     */
    @GetMapping("/mes-projets")
    public ResponseEntity<List<ProjetResponse>> getMesProjets() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            log.warn("GET /api/employe/projets/mes-projets - No authenticated user");
            return ResponseEntity.status(401).build();
        }

        log.info("GET /api/employe/projets/mes-projets - User: {}", currentUserId);

        // Get all TravaillerProjet entries for this employee
        List<TravaillerProjet> travaillerProjets = travaillerProjetRepository.findByEmployeId(currentUserId);
        
        // Extract projects and convert to responses
        List<ProjetResponse> projets = travaillerProjets.stream()
                .map(TravaillerProjet::getProjet)
                .distinct()
                .map(projetService::mapToResponse)
                .collect(Collectors.toList());

        log.info("Found {} projects for employee {}", projets.size(), currentUserId);
        return ResponseEntity.ok(projets);
    }

    /**
     * Get a specific project by ID - only if the employee is assigned to it
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjetResponse> getProjetById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            log.warn("GET /api/employe/projets/{} - No authenticated user", id);
            return ResponseEntity.status(401).build();
        }

        log.info("GET /api/employe/projets/{} - User: {}", id, currentUserId);

        // Check if employee is assigned to this project
        List<TravaillerProjet> assignments = travaillerProjetRepository.findByEmployeId(currentUserId);
        boolean hasAccess = assignments.stream()
                .anyMatch(tp -> tp.getProjet().getId().equals(id));

        if (!hasAccess) {
            log.warn("Employee {} attempted to access project {} without assignment", currentUserId, id);
            return ResponseEntity.status(403).build();
        }

        Optional<Projet> projet = projetRepository.findById(id);
        if (projet.isPresent()) {
            return ResponseEntity.ok(projetService.mapToResponse(projet.get()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Check if the current user is chef de projet for a specific project
     */
    @GetMapping("/{id}/is-chef")
    public ResponseEntity<Boolean> isChefDeProjet(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        // Vérifier via la table travailler_projet avec le champ est_chef
        Optional<TravaillerProjet> tp = travaillerProjetRepository
            .findByEmployeIdAndProjetId(currentUserId, id);

        boolean isChef = tp.isPresent() && tp.get().isEstChef() != null && tp.get().isEstChef();
        return ResponseEntity.ok(isChef);
    }

    /**
     * Deposit a project (file or link) - POST /soumettre pour éviter les problèmes de PATCH
     */
    @PostMapping(value = "/{id}/soumettre",
                 consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            MediaType.ALL_VALUE})
    public ResponseEntity<?> soumettreDepotProjet(
            @PathVariable Long id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lien", required = false) String lien,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier) {

        System.out.println("=== POST SOUMETTRE PROJET ID: " + id + " ===");
        System.out.println("Type: " + type);
        System.out.println("Lien: " + lien);
        System.out.println("Fichier: " + (fichier != null ? fichier.getOriginalFilename() : "null"));

        // Déterminer le type automatiquement si non fourni
        if (type == null) {
            type = (fichier != null) ? "fichier" : (lien != null ? "lien" : null);
            System.out.println("Type auto-détecté: " + type);
        }

        try {
            // Check if user is chef de projet for this project
            securityService.checkCurrentUserIsChefDeProjet(id);

            com.projet.dto.DepotRequest depotRequest = new com.projet.dto.DepotRequest();
            depotRequest.setType(type);
            depotRequest.setLien(lien);
            depotRequest.setNomFichier(fichier != null ? fichier.getOriginalFilename() : null);

            ProjetResponse updated = projetService.deposerProjet(id, depotRequest, fichier);
            System.out.println("=== DÉPÔT PROJET RÉUSSI ===");
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors du dépôt du projet: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Erreur IO lors du dépôt du projet: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors du traitement du fichier");
        } catch (Exception e) {
            log.error("Erreur inattendue lors du dépôt du projet: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors du dépôt du projet");
        }
    }

    /**
     * Check if a project has a depot
     */
    @GetMapping("/{id}/depot-exists")
    public ResponseEntity<Map<String, Object>> hasDepot(@PathVariable Long id) {
        log.info("GET /api/employe/projets/{}/depot-exists - Vérification dépôt", id);

        try {
            boolean hasDepot = projetService.hasDepot(id);
            Map<String, Object> response = new HashMap<>();
            response.put("hasDepot", hasDepot);
            response.put("projetId", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du dépôt: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update project status
     */
    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("PATCH /api/employe/projets/{}/statut - Mise à jour du statut", id);

        try {
            // Check if user is chef de projet for this project
            securityService.checkCurrentUserIsChefDeProjet(id);

            String statut = request.get("statut");
            if (statut == null || statut.isEmpty()) {
                return ResponseEntity.badRequest().body("Le statut est requis");
            }

            ProjetResponse updated = projetService.updateStatut(id, statut);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour du statut: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour du statut: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour du statut");
        }
    }
}
