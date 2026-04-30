package com.projet.controller;

import com.projet.dto.AdminRessourceRequest;
import com.projet.entity.Ressource;
import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import com.projet.repository.RessourceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminRessourceController {

    private final RessourceRepository ressourceRepository;

    // Créer une ressource
    // statut = ACTIVE, situation = DISPONIBLE par défaut
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Ressource> creerRessource(@Valid @RequestBody AdminRessourceRequest request) {
        log.info("POST /api/admin/ressources - Création d'une ressource");
        
        Ressource ressource = new Ressource();
        ressource.setNom(request.getNom());
        ressource.setDescription(request.getDescription());
        ressource.setType(request.getType());
        ressource.setStatut(StatutRessource.ACTIVE);       // toujours ACTIVE à la création
        ressource.setSituation(SituationRessource.DISPONIBLE); // toujours DISPONIBLE à la création
        ressource.setDateCreation(LocalDateTime.now());
        
        Ressource saved = ressourceRepository.save(ressource);
        log.info("Ressource créée avec ID: {}", saved.getId());
        
        return ResponseEntity.ok(saved);
    }

    // Lire toutes les ressources (admin voit tout)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ressource>> getAllRessources() {
        log.info("GET /api/admin/ressources - Récupération de toutes les ressources");
        List<Ressource> ressources = ressourceRepository.findAll();
        return ResponseEntity.ok(ressources);
    }

    // Modifier une ressource (nom, description, type)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Ressource> modifierRessource(
            @PathVariable Long id,
            @Valid @RequestBody AdminRessourceRequest request) {
        
        log.info("PUT /api/admin/ressources/{} - Modification d'une ressource", id);
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec ID: " + id));
        
        ressource.setNom(request.getNom());
        ressource.setDescription(request.getDescription());
        ressource.setType(request.getType());
        // NE PAS modifier statut ni situation ici
        
        Ressource saved = ressourceRepository.save(ressource);
        log.info("Ressource {} modifiée avec succès", id);
        
        return ResponseEntity.ok(saved);
    }

    // Supprimer une ressource
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerRessource(@PathVariable Long id) {
        log.info("DELETE /api/admin/ressources/{} - Suppression d'une ressource", id);
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec ID: " + id));
        
        // Vérifier que la ressource n'est pas demandée
        if (ressource.getSituation() == SituationRessource.DEMANDE) {
            return ResponseEntity.badRequest().build();
        }
        
        ressourceRepository.deleteById(id);
        log.info("Ressource {} supprimée avec succès", id);
        
        return ResponseEntity.ok().build();
    }

    // Changer le statut ACTIVE/NON_ACTIVE
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Ressource> changerStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        log.info("PATCH /api/admin/ressources/{}/statut - Changement de statut", id);
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec ID: " + id));
        
        String nouveauStatut = body.get("statut");
        if (nouveauStatut == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            StatutRessource statut = StatutRessource.valueOf(nouveauStatut.toUpperCase());
            ressource.setStatut(statut);
            Ressource saved = ressourceRepository.save(ressource);
            log.info("Statut de la ressource {} changé en {}", id, statut);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.error("Statut invalide: {}", nouveauStatut);
            return ResponseEntity.badRequest().build();
        }
    }

    // Remettre la situation à DISPONIBLE (demande traitée)
    @PatchMapping("/{id}/liberer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Ressource> libererRessource(@PathVariable Long id) {
        log.info("PATCH /api/admin/ressources/{}/liberer - Libération d'une ressource", id);
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec ID: " + id));
        
        ressource.setSituation(SituationRessource.DISPONIBLE);
        ressource.setEmployeDemandeur(null);
        ressource.setDateDemande(null);
        
        Ressource saved = ressourceRepository.save(ressource);
        log.info("Ressource {} libérée avec succès", id);
        
        return ResponseEntity.ok(saved);
    }
}
