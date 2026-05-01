package com.projet.controller;

import com.projet.dto.RessourceRequest;
import com.projet.dto.RessourceResponse;
import com.projet.dto.RessourceDisponibleDTO;
import com.projet.service.RessourceService;
import com.projet.repository.RessourceRepository;
import com.projet.entity.Ressource;
import com.projet.entity.Employe;
import com.projet.repository.EmployeRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class RessourceController {

    private final RessourceService ressourceService;
    private final RessourceRepository ressourceRepository;
    private final EmployeRepository employeRepository;

    // ========================
    // CRUD — ADMIN UNIQUEMENT
    // ========================

    // ========================
    // CRUD — ADMIN UNIQUEMENT
    // ========================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RessourceResponse>> getAllRessources() {
        log.info("GET /api/ressources - Liste de toutes les ressources pour admin");
        List<RessourceResponse> ressources = ressourceService.getAllForAdmin();
        return ResponseEntity.ok(ressources);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> createRessource(@Valid @RequestBody RessourceRequest request) {
        log.info("POST /api/ressources - Création d'une ressource");
        log.info("Payload reçu: {}", request);
        log.info("Nom: {}, Description: {}, Prix: {}, Date début: {}, Date fin: {}", 
                request.getNom(), request.getDescription(), request.getPrix(), 
                request.getDateDebut(), request.getDateFin());
        
        try {
            RessourceResponse created = ressourceService.createRessource(request);
            log.info("Ressource créée avec succès: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Erreur lors de la création de la ressource: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> getRessourceById(@PathVariable Long id) {
        log.info("GET /api/ressources/{} - Détail d'une ressource", id);
        RessourceResponse ressource = ressourceService.getRessourceById(id);
        return ResponseEntity.ok(ressource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> updateRessource(
            @PathVariable Long id,
            @Valid @RequestBody RessourceRequest request) {
        log.info("PUT /api/ressources/{} - Mise à jour d'une ressource", id);
        RessourceResponse updated = ressourceService.updateRessource(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRessource(@PathVariable Long id) {
        log.info("DELETE /api/ressources/{} - Suppression d'une ressource", id);
        ressourceService.deleteRessource(id);
        return ResponseEntity.noContent().build();
    }

    
    // Handler global pour les erreurs de validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Erreur de validation détectée: {}", ex.getMessage());
        
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.error("Champs en erreur: {}", errors);
        return ResponseEntity.badRequest().body("Erreur de validation: " + errors);
    }

    // ========================
    // MAPPING DTO
    // ========================

    private RessourceDisponibleDTO mapToDisponibleDTO(Ressource ressource) {
        RessourceDisponibleDTO dto = new RessourceDisponibleDTO();
        dto.setId(ressource.getId());
        dto.setNom(ressource.getNom());
        dto.setDescription(ressource.getDescription());
        dto.setStatut(ressource.getStatut().name());
        dto.setSituation(ressource.getSituation().name());
        dto.setPrix(ressource.getPrix());
        dto.setDateDebut(ressource.getDateDebut());
        dto.setDateFin(ressource.getDateFin());
        
        // Informations sur l'employé demandeur si applicable
        if (ressource.getEmployeDemandeur() != null) {
            dto.setEmployeDemandeur(ressource.getEmployeDemandeur().getPrenom() + " " + ressource.getEmployeDemandeur().getNom());
        }
        
        dto.setDateDemande(ressource.getDateDemande());
        
        return dto;
    }
}
