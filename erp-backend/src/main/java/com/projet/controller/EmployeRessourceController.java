package com.projet.controller;

import com.projet.dto.DemandeRessourceRequest;
import com.projet.entity.DemandeRessource;
import com.projet.entity.Employe;
import com.projet.entity.Ressource;
import com.projet.enums.StatutRessource;
import com.projet.repository.EmployeRepository;
import com.projet.repository.RessourceRepository;
import com.projet.repository.DemandeRessourceRepository;
import com.projet.service.DemandeRessourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employe/ressources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeRessourceController {

    private final RessourceRepository ressourceRepository;
    private final EmployeRepository employeRepository;
    private final DemandeRessourceRepository demandeRessourceRepository;
    private final DemandeRessourceService demandeService;

    // Voir toutes les ressources ACTIVES avec flag dejaDemandeParMoi
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYE')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRessourcesActives(Authentication authentication) {

        try {
            System.out.println("=== GET /api/employe/ressources ===");

            // 1. Récupérer l'employé connecté
            String email = authentication.getName();
            System.out.println("Email: " + email);

            Employe employe = employeRepository
                .findByEmail(email)
                .orElseThrow(() -> 
                    new RuntimeException("Employé non trouvé: " + email));

            System.out.println("Employé trouvé: " + employe.getId());

            // 2. Récupérer toutes les ressources ACTIVE
            List<Ressource> ressources = ressourceRepository
                .findByStatut(StatutRessource.ACTIVE);

            System.out.println("Ressources trouvées: " + ressources.size());

            // 3. Mapper avec flag dejaDemandeParMoi et nombre de demandes
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

                    // Nombre de demandes EN_ATTENTE pour cette ressource
                    long nombreDemandes = demandeRessourceRepository
                        .countByRessourceIdAndStatutDemande(
                            r.getId(), com.projet.enums.StatutDemande.EN_ATTENTE);
                    dto.put("nombreDemandes", nombreDemandes);

                    // Est-ce que CET employé a déjà demandé ?
                    boolean dejaDemandeParMoi = demandeRessourceRepository
                        .existsByRessourceIdAndEmployeIdAndStatutDemande(
                            r.getId(),
                            employe.getId(),
                            com.projet.enums.StatutDemande.EN_ATTENTE);
                    dto.put("dejaDemandeParMoi", dejaDemandeParMoi);

                    return dto;
                })
                .collect(Collectors.toList());

            System.out.println("=== SUCCÈS ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== ERREUR: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("erreur", e.getMessage()));
        }
    }

    // Demander une ressource
    @PostMapping("/{id}/demander")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<?> demanderRessource(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("POST /api/employe/ressources/{}/demander - Demande de ressource", id);

        try {
            String email = authentication.getName();
            Employe employe = employeRepository
                .findByEmail(email).orElseThrow();

            Ressource ressource = ressourceRepository
                .findById(id).orElseThrow();

            // Vérification 1 : ressource active
            if (ressource.getStatut() != StatutRessource.ACTIVE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Ressource non active"));
            }

            // Vérification 2 : cet employé n'a pas déjà demandé
            boolean dejaDemandeParMoi = demandeRessourceRepository
                .existsByRessourceIdAndEmployeIdAndStatutDemande(
                    id,
                    employe.getId(),
                    com.projet.enums.StatutDemande.EN_ATTENTE);

            if (dejaDemandeParMoi) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erreur",
                        "Vous avez déjà demandé cette ressource"));
            }

            // Créer la demande dans l'historique
            DemandeRessource demande = new DemandeRessource();
            demande.setRessource(ressource);
            demande.setEmploye(employe);
            demande.setDateDemande(java.time.LocalDateTime.now());
            demande.setStatutDemande(com.projet.enums.StatutDemande.EN_ATTENTE);
            demandeRessourceRepository.save(demande);

            // Calculer le nouveau nombreDemandes
            long nombreDemandes = demandeRessourceRepository
                .countByRessourceIdAndStatutDemande(
                    id, com.projet.enums.StatutDemande.EN_ATTENTE);

            log.info("Ressource {} demandée par l'employé {}", id, employe.getId());
            return ResponseEntity.ok(Map.of(
                "message", "Ressource demandée avec succès",
                "nombreDemandes", nombreDemandes
            ));

        } catch (Exception e) {
            log.warn("Erreur lors de la demande de ressource {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }

    // Annuler sa propre demande
    @DeleteMapping("/{id}/annuler")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<?> annulerDemande(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("DELETE /api/employe/ressources/{}/annuler - Annulation de demande", id);

        try {
            String email = authentication.getName();
            Employe employe = employeRepository
                .findByEmail(email).orElseThrow();

            // Trouver la demande EN_ATTENTE de cet employé
            DemandeRessource demande = demandeRessourceRepository
                .findByRessourceIdAndEmployeIdAndStatutDemande(
                    id,
                    employe.getId(),
                    com.projet.enums.StatutDemande.EN_ATTENTE)
                .orElseThrow(() -> new RuntimeException(
                    "Aucune demande en attente trouvée"));

            // Annuler la demande
            demande.setStatutDemande(com.projet.enums.StatutDemande.ANNULEE);
            demandeRessourceRepository.save(demande);

            // Compter les demandes restantes EN_ATTENTE
            long restantes = demandeRessourceRepository
                .countByRessourceIdAndStatutDemande(
                    id, com.projet.enums.StatutDemande.EN_ATTENTE);

            // Si plus aucune demande → ressource libérée
            // Pas besoin de modifier la ressource, la situation n'existe plus

            log.info("Demande de la ressource {} annulée par l'employé {}", id, employe.getId());
            return ResponseEntity.ok(Map.of(
                "message", "Demande annulée avec succès",
                "nombreDemandes", restantes
            ));

        } catch (Exception e) {
            log.warn("Erreur lors de l'annulation de la demande {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }
}
