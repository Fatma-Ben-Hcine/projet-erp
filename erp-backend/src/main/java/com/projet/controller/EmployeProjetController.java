package com.projet.controller;

import com.projet.dto.ProjetResponse;
import com.projet.entity.Projet;
import com.projet.entity.TravaillerProjet;
import com.projet.entity.Utilisateur;
import com.projet.repository.ProjetRepository;
import com.projet.repository.TravaillerProjetRepository;
import com.projet.service.ProjetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

        boolean isChef = projetRepository.isChefDeProjet(id, currentUserId);
        return ResponseEntity.ok(isChef);
    }
}
