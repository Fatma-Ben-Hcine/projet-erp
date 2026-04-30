package com.projet.service;

import com.projet.dto.RessourceRequest;
import com.projet.dto.RessourceResponse;
import com.projet.dto.RessourceDisponibleDTO;
import com.projet.entity.Ressource;
import com.projet.entity.Projet;
import com.projet.entity.Employe;
import com.projet.entity.DemandeRessource;
import com.projet.enums.StatutRessource;
import com.projet.enums.SituationRessource;
import com.projet.repository.RessourceRepository;
import com.projet.repository.ProjetRepository;
import com.projet.repository.DemandeRessourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RessourceService {

    private final RessourceRepository ressourceRepository;
    private final DemandeRessourceRepository demandeRessourceRepository;

    // ========================
    // CRUD OPÉRATIONS
    // ========================

    @Transactional
    public RessourceResponse createRessource(RessourceRequest request) {
        log.info("Création d'une nouvelle ressource: {}", request.getNom());

        Ressource ressource = new Ressource();
        ressource.setNom(request.getNom());
        ressource.setDescription(request.getDescription());
        ressource.setPrix(request.getPrix());
        ressource.setStatut(request.getStatut());
        ressource.setSituation(SituationRessource.NON_DEMANDE); // Valeur par défaut
        ressource.setStatutForceManuel(request.getStatutForceManuel());

        // Dates d'abonnement (optionnelles)
        ressource.setDateDebutAbonnement(request.getDateDebutAbonnement());
        ressource.setDateFinAbonnement(request.getDateFinAbonnement());

        // Calcul automatique du statut si dates renseignées
        if (ressource.isEstAbonne() && !ressource.isStatutForceManuel()) {
            updateStatutBasedOnDates(ressource);
        }

        Ressource saved = ressourceRepository.save(ressource);
        log.info("Ressource créée avec ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public RessourceResponse updateRessource(Long id, RessourceRequest request) {
        log.info("Mise à jour de la ressource ID: {}", id);

        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + id));

        ressource.setNom(request.getNom());
        ressource.setDescription(request.getDescription());
        ressource.setPrix(request.getPrix());
        ressource.setStatutForceManuel(request.getStatutForceManuel());

        // Si l'admin force manuellement le statut
        if (Boolean.TRUE.equals(request.getStatutForceManuel())) {
            ressource.setStatut(request.getStatut());
            log.info("Statut forcé manuellement pour la ressource {}", id);
        } else {
            // Sinon, réinitialiser le flag et appliquer la logique automatique
            ressource.setStatut(request.getStatut());
        }

        // Mise à jour des dates
        ressource.setDateDebutAbonnement(request.getDateDebutAbonnement());
        ressource.setDateFinAbonnement(request.getDateFinAbonnement());

        // Recalculer le statut si dates renseignées et pas de forçage manuel
        if (ressource.isEstAbonne() && !ressource.isStatutForceManuel()) {
            updateStatutBasedOnDates(ressource);
        }

        Ressource updated = ressourceRepository.save(ressource);
        log.info("Ressource mise à jour: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<RessourceResponse> getAllRessources() {
        log.debug("Récupération de toutes les ressources");

        List<Ressource> ressources = ressourceRepository.findAll();

        // Recalculer les statuts avant de renvoyer
        ressources.forEach(this::recalculateStatutIfNeeded);

        return ressources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RessourceResponse getRessourceById(Long id) {
        log.debug("Récupération de la ressource ID: {}", id);

        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + id));

        // Recalculer le statut avant de renvoyer
        recalculateStatutIfNeeded(ressource);

        return mapToResponse(ressource);
    }

    @Transactional
    public void deleteRessource(Long id) {
        log.info("Suppression de la ressource ID: {}", id);

        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + id));

        ressourceRepository.delete(ressource);
        log.info("Ressource supprimée: {}", id);
    }

    // ========================
    // LOGIQUE MÉTIER — STATUT AUTOMATIQUE
    // ========================

    /**
     * Met à jour le statut automatiquement selon les dates d'abonnement.
     * Ne s'applique que si le statut n'est pas forcé manuellement.
     */
    private void updateStatutBasedOnDates(Ressource ressource) {
        if (ressource.isStatutForceManuel()) {
            log.debug("Statut manuellement forcé pour la ressource {}, pas de recalcul", ressource.getId());
            return;
        }

        if (!ressource.isEstAbonne()) {
            return; // Pas de dates, pas de recalcul
        }

        LocalDate now = LocalDate.now();

        if (now.isBefore(ressource.getDateDebutAbonnement())) {
            // Abonnement pas encore commencé
            ressource.setStatut(StatutRessource.NON_ACTIVE);
            log.debug("Ressource {} : abonnement futur, statut = NON_ACTIVE", ressource.getId());
        } else if (now.isAfter(ressource.getDateFinAbonnement())) {
            // Abonnement expiré
            ressource.setStatut(StatutRessource.NON_ACTIVE);
            log.debug("Ressource {} : abonnement expiré, statut = NON_ACTIVE", ressource.getId());
        } else {
            // Abonnement en cours
            ressource.setStatut(StatutRessource.ACTIVE);
            log.debug("Ressource {} : abonnement en cours, statut = ACTIVE", ressource.getId());
        }
    }

    /**
     * Recalculer le statut si nécessaire (dates renseignées et pas de forçage manuel)
     */
    private void recalculateStatutIfNeeded(Ressource ressource) {
        if (ressource.isEstAbonne() && !ressource.isStatutForceManuel()) {
            updateStatutBasedOnDates(ressource);
        }
    }

    // ========================
    // CRON JOB — MISE À JOUR QUOTIDIENNE
    // ========================

    /**
     * Job planifié quotidien à 00:01 pour mettre à jour les statuts des ressources.
     * S'exécute uniquement sur les ressources avec dates d'abonnement renseignées
     * et sans forçage manuel du statut.
     */
    @Scheduled(cron = "0 1 0 * * ?") // Tous les jours à 00:01
    @Transactional
    public void scheduledStatutUpdate() {
        log.info("=== DÉBUT DU CRON JOB : Mise à jour des statuts de ressources ===");

        List<Ressource> ressources = ressourceRepository.findAllWithAbonnementDates();
        log.info("Nombre de ressources avec abonnement : {}", ressources.size());

        int updatedCount = 0;
        for (Ressource ressource : ressources) {
            if (!ressource.isStatutForceManuel()) {
                StatutRessource oldStatut = ressource.getStatut();
                updateStatutBasedOnDates(ressource);

                if (oldStatut != ressource.getStatut()) {
                    updatedCount++;
                    log.info("Ressource {} : statut changé de {} à {}",
                            ressource.getId(), oldStatut, ressource.getStatut());
                }
            }
        }

        ressourceRepository.saveAll(ressources);
        log.info("=== FIN DU CRON JOB : {} ressources mises à jour ===", updatedCount);
    }

    // ========================
    // RECHERCHE AVANCÉE
    // ========================

    @Transactional(readOnly = true)
    public List<RessourceDisponibleDTO> getRessourcesDisponiblesPourEmploye(Employe employe) {
        log.debug("Récupération des ressources disponibles pour l'employé: {}", employe.getEmail());

        // Toutes les ressources actives (sans filtre sur situation)
        List<Ressource> ressources = ressourceRepository.findByStatut(StatutRessource.ACTIVE);

        return ressources.stream()
                .map(ressource -> {
                    RessourceDisponibleDTO dto = mapToDisponibleDTO(ressource);
                    
                    // Vérifier si cet employé a déjà demandé cette ressource
                    boolean dejaDemande = demandeRessourceRepository
                            .existsByEmployeAndRessource(employe, ressource);
                    dto.setDejaDemandeParMoi(dejaDemande);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ========================
    // MAPPING DTO
    // ========================

    private RessourceResponse mapToResponse(Ressource ressource) {
        RessourceResponse response = new RessourceResponse();
        response.setId(ressource.getId());
        response.setNom(ressource.getNom());
        response.setDescription(ressource.getDescription());
        response.setSituation(ressource.getSituation());
        response.setStatut(ressource.getStatut());
        response.setPrix(ressource.getPrix());
        response.setDateDebutAbonnement(ressource.getDateDebutAbonnement());
        response.setDateFinAbonnement(ressource.getDateFinAbonnement());
        response.setEstAbonne(ressource.isEstAbonne());
        response.setStatutForceManuel(ressource.isStatutForceManuel());

        // Informations calculées
        response.setAbonnementExpire(ressource.isAbonnementExpire());
        response.setAbonnementEnCours(ressource.isAbonnementEnCours());
        response.setAbonnementFutur(ressource.isAbonnementFutur());

        // Informations du projet (si présent)
        if (ressource.getProjet() != null) {
            RessourceResponse.ProjetInfo projetInfo = new RessourceResponse.ProjetInfo();
            projetInfo.setId(ressource.getProjet().getId());
            projetInfo.setNom(ressource.getProjet().getNom());
            response.setProjet(projetInfo);
        }

        return response;
    }

    private RessourceDisponibleDTO mapToDisponibleDTO(Ressource ressource) {
        RessourceDisponibleDTO dto = new RessourceDisponibleDTO();
        dto.setId(ressource.getId());
        dto.setNom(ressource.getNom());
        dto.setDescription(ressource.getDescription());
        dto.setType(ressource.getType());
        dto.setPrix(ressource.getPrix());
        dto.setStatut(ressource.getStatut().name());
        dto.setDateDebut(ressource.getDateDebutAbonnement());
        dto.setDateFin(ressource.getDateFinAbonnement());
        dto.setStatutForceManuel(ressource.isStatutForceManuel() ? "OUI" : "NON");
        dto.setAbonnementEnCours(ressource.isAbonnementEnCours());
        dto.setAbonnementFutur(ressource.isAbonnementFutur());
        dto.setSituation(ressource.getSituation().name());
        
        // Le flag dejaDemandeParMoi sera set par la méthode appelante
        dto.setDejaDemandeParMoi(false);

        return dto;
    }
}
