package com.projet.controller;

import com.projet.dto.DemandeRessourceRequest;
import com.projet.dto.DemandeRessourceResponse;
import com.projet.dto.DemandeMultipleRequest;
import com.projet.service.DemandeRessourceService;
import com.projet.service.RessourceService;
import com.projet.entity.Ressource;
import com.projet.entity.DemandeRessource;
import com.projet.repository.RessourceRepository;
import com.projet.repository.DemandeRessourceRepository;
import com.projet.enums.StatutRessource;
import com.projet.enums.SituationRessource;
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
import java.util.Map;

@RestController
@RequestMapping("/api/demandes-ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class DemandeRessourceController {

    private final DemandeRessourceService demandeService;
    private final RessourceService ressourceService;
    private final RessourceRepository ressourceRepository;
    private final DemandeRessourceRepository demandeRessourceRepository;
    private final EmployeRepository employeRepository;

    // ========================
    // EMPLOYÉ — DEMANDES MULTIPLES
    // ========================

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYE', 'ADMIN')")
    public ResponseEntity<?> createDemandesMultiples(
            @Valid @RequestBody DemandeMultipleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername(); // = sub du JWT
        log.info("POST /api/demandes-ressources - Demandes multiples par {}", email);
        
        // Récupérer l'employé par son email
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'email: " + email));
        
        log.info("Employé trouvé: {} (ID: {})", employe.getEmail(), employe.getId());
        
        int successCount = 0;
        int errorCount = 0;
        StringBuilder errors = new StringBuilder();
        
        for (Long ressourceId : request.getRessourceIds()) {
            try {
                // Vérifier que la ressource existe et est ACTIVE
                Ressource ressource = ressourceRepository.findById(ressourceId)
                        .orElseThrow(() -> new RuntimeException("Ressource non trouvée: " + ressourceId));
                
                if (ressource.getStatut() != StatutRessource.ACTIVE) {
                    errors.append("Ressource ").append(ressourceId).append(" n'est pas active. ");
                    errorCount++;
                    continue;
                }
                
                // Vérifier que cet employé n'a pas déjà demandé cette ressource
                if (demandeRessourceRepository.findByRessourceAndEmploye(ressource, employe).isPresent()) {
                    errors.append("Ressource ").append(ressourceId).append(" déjà demandée. ");
                    errorCount++;
                    continue;
                }
                
                // Créer la demande
                DemandeRessource demande = new DemandeRessource();
                demande.setRessource(ressource);
                demande.setEmploye(employe);
                demandeRessourceRepository.save(demande);
                
                // Si c'est la première demande pour cette ressource, mettre la situation à DEMANDE
                List<DemandeRessource> demandesExistantes = demandeRessourceRepository.findByRessourceId(ressourceId);
                if (demandesExistantes.size() == 1) { // Juste créée, donc c'est la première
                    ressource.setSituation(SituationRessource.DEMANDE);
                    ressourceRepository.save(ressource);
                }
                
                successCount++;
                log.info("Demande créée pour la ressource {} par l'employé {}", ressourceId, email);
                
            } catch (Exception e) {
                log.error("Erreur lors de la création de la demande pour la ressource {}: {}", ressourceId, e.getMessage());
                errors.append("Erreur ressource ").append(ressourceId).append(": ").append(e.getMessage()).append(". ");
                errorCount++;
            }
        }
        
        String message = String.format("Demandes traitées: %d succès, %d erreurs", successCount, errorCount);
        if (errorCount > 0) {
            message += ". Erreurs: " + errors.toString();
        }
        
        return ResponseEntity.ok(Map.of(
            "message", message,
            "succes", successCount,
            "erreurs", errorCount
        ));
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

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeRessourceResponse>> getDemandesEnAttente() {
        log.info("GET /api/demandes-ressources/en-attente - Demandes en attente");
        List<DemandeRessourceResponse> demandes = demandeService.getDemandesEnAttente();
        return ResponseEntity.ok(demandes);
    }

    @PutMapping("/{id}/approuver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeRessourceResponse> approuverDemande(@PathVariable Long id) {
        log.info("PUT /api/demandes-ressources/{}/approuver - Approuver la demande", id);
        DemandeRessourceResponse updated = demandeService.approuverDemande(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> annulerDemande(@PathVariable Long id) {
        log.info("PUT /api/demandes-ressources/{}/annuler - Annuler la demande", id);
        // Pour l'admin, on utilise l'employéId = null pour indiquer que c'est l'admin qui annule
        demandeService.annulerDemande(id, null);
        return ResponseEntity.noContent().build();
    }
}
