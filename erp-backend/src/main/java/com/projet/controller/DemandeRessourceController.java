package com.projet.controller;

import com.projet.dto.DemandeRessourceRequest;
import com.projet.dto.DemandeRessourceResponse;
import com.projet.service.DemandeRessourceService;
import com.projet.entity.Employe;
import com.projet.repository.EmployeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes-ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class DemandeRessourceController {

    private final DemandeRessourceService demandeService;
    private final EmployeRepository employeRepository;

    // ========================
    // EMPLOYÉ — CRÉER UNE DEMANDE
    // ========================

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYE', 'ADMIN')")
    public ResponseEntity<DemandeRessourceResponse> createDemande(
            @Valid @RequestBody DemandeRessourceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername(); // = sub du JWT
        log.info("POST /api/demandes-ressources - Création d'une demande par {}", email);
        
        // Récupérer l'employé par son email
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'email: " + email));
        
        log.info("Employé trouvé: {} (ID: {})", employe.getEmail(), employe.getId());
        
        // Utiliser l'employeId récupéré
        DemandeRessourceResponse created = demandeService.createDemande(request, employe.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/employe/{employeId}")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'ADMIN')")
    public ResponseEntity<List<DemandeRessourceResponse>> getDemandesByEmploye(@PathVariable Long employeId) {
        log.info("GET /api/demandes-ressources/employe/{} - Liste des demandes d'un employé", employeId);
        List<DemandeRessourceResponse> demandes = demandeService.getDemandesByEmploye(employeId);
        return ResponseEntity.ok(demandes);
    }

    // ========================
    // ADMIN — GÉRER LES DEMANDES
    // ========================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeRessourceResponse>> getAllDemandes() {
        log.info("GET /api/demandes-ressources - Liste de toutes les demandes (admin)");
        List<DemandeRessourceResponse> demandes = demandeService.getAllDemandes();
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/non-traitees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeRessourceResponse>> getDemandesNonTraitees() {
        log.info("GET /api/demandes-ressources/non-traitees - Demandes non traitées");
        List<DemandeRessourceResponse> demandes = demandeService.getDemandesNonTraitees();
        return ResponseEntity.ok(demandes);
    }

    @PutMapping("/{id}/traiter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeRessourceResponse> marquerDemandeTraitee(@PathVariable Long id) {
        log.info("PUT /api/demandes-ressources/{}/traiter - Marquer comme traitée", id);
        DemandeRessourceResponse updated = demandeService.marquerDemandeTraitee(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDemande(@PathVariable Long id) {
        log.info("DELETE /api/demandes-ressources/{} - Suppression d'une demande", id);
        demandeService.deleteDemande(id);
        return ResponseEntity.noContent().build();
    }
}
