package com.projet.controller;

import com.projet.dto.RessourceRequest;
import com.projet.dto.RessourceResponse;
import com.projet.entity.Ressource;
import com.projet.enums.StatutRessource;
import com.projet.enums.StatutDemande;
import com.projet.service.RessourceService;
import com.projet.repository.RessourceRepository;
import com.projet.repository.DemandeRessourceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminRessourceController {

    private final RessourceService ressourceService;
    private final RessourceRepository ressourceRepository;
    private final DemandeRessourceRepository demandeRessourceRepository;

    // Créer une ressource
    // statut = ACTIVE par défaut
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> creerRessource(@Valid @RequestBody RessourceRequest request) {
        log.info("POST /api/admin/ressources - Création d'une ressource");
        
        RessourceResponse response = ressourceService.createRessource(request);
        log.info("Ressource créée avec succès");
        
        return ResponseEntity.ok(response);
    }

    // Lire toutes les ressources (admin voit tout)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllRessources() {
        log.info("GET /api/admin/ressources - Récupération de toutes les ressources");
        
        List<Ressource> ressources = ressourceRepository.findAll();

        List<Map<String, Object>> response = ressources.stream()
            .map(r -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", r.getId());
                dto.put("nom", r.getNom());
                dto.put("description", r.getDescription());
                dto.put("statut", r.getStatut());
                dto.put("prix", r.getPrix());
                dto.put("dateDebutAbonnement", r.getDateDebutAbonnement());
                dto.put("dateFinAbonnement", r.getDateFinAbonnement());

                // ← CALCUL DEPUIS LA TABLE demande_ressource
                long nombreDemandes = demandeRessourceRepository
                    .countByRessourceIdAndStatutDemande(
                        r.getId(),
                        StatutDemande.EN_ATTENTE);
                dto.put("nombreDemandes", nombreDemandes);

                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Modifier une ressource (nom, description, prix, dates)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> modifierRessource(
            @PathVariable Long id,
            @Valid @RequestBody RessourceRequest request) {
        
        log.info("PUT /api/admin/ressources/{} - Modification d'une ressource", id);
        
        RessourceResponse response = ressourceService.updateRessource(id, request);
        log.info("Ressource {} modifiée avec succès", id);
        
        return ResponseEntity.ok(response);
    }

    // Supprimer une ressource
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerRessource(@PathVariable Long id) {
        log.info("DELETE /api/admin/ressources/{} - Suppression d'une ressource", id);
        
        ressourceService.deleteRessource(id);
        log.info("Ressource {} supprimée avec succès", id);
        
        return ResponseEntity.ok().build();
    }

    
    // Changer le statut d'une ressource
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> changerStatutRessource(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        
        String nouveauStatut = request.get("statut");
        log.info("PATCH /api/admin/ressources/{}/statut - Changement de statut vers {}", id, nouveauStatut);
        
        try {
            StatutRessource statut = StatutRessource.valueOf(nouveauStatut);
            ressourceService.changerStatutRessource(id, statut);
            log.info("Statut de la ressource {} changé avec succès vers {}", id, nouveauStatut);
            
            // Retourner le nouveau statut pour confirmation
            return ResponseEntity.ok(Map.of(
                "message", "Statut mis à jour avec succès",
                "statut", statut.name()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Statut invalide: {}", nouveauStatut);
            return ResponseEntity.badRequest().build();
        }
    }

    
    // Obtenir les détails d'une ressource
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RessourceResponse> getRessourceById(@PathVariable Long id) {
        log.info("GET /api/admin/ressources/{} - Récupération d'une ressource", id);
        
        RessourceResponse response = ressourceService.getRessourceById(id);
        return ResponseEntity.ok(response);
    }
}
