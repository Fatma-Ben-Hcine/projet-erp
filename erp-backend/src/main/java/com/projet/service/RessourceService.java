package com.projet.service;

import com.projet.dto.RessourceRequest;
import com.projet.dto.RessourceResponse;
import com.projet.dto.RessourceDisponibleDTO;
import com.projet.entity.Ressource;
import com.projet.entity.Employe;
import com.projet.entity.DemandeRessource;
import com.projet.enums.StatutRessource;
import com.projet.repository.RessourceRepository;
import com.projet.repository.DemandeRessourceRepository;
import com.projet.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RessourceService {

    private final RessourceRepository ressourceRepository;
    private final DemandeRessourceRepository demandeRessourceRepository;
    private final EmployeRepository employeRepository;

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
        ressource.setDateDebutAbonnement(request.getDateDebutAbonnement());
        ressource.setDateFinAbonnement(request.getDateFinAbonnement());
        ressource.setStatut(StatutRessource.ACTIVE); // Valeur par défaut

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
        ressource.setDateDebutAbonnement(request.getDateDebutAbonnement());
        ressource.setDateFinAbonnement(request.getDateFinAbonnement());
        // Ne pas modifier statut - géré par les actions dédiées

        Ressource updated = ressourceRepository.save(ressource);
        log.info("Ressource mise à jour: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<RessourceResponse> getAllRessources() {
        log.debug("Récupération de toutes les ressources");

        List<Ressource> ressources = ressourceRepository.findAll();

        return ressources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RessourceResponse getRessourceById(Long id) {
        log.debug("Récupération de la ressource ID: {}", id);

        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + id));

        return mapToResponse(ressource);
    }

    @Transactional
    public void deleteRessource(Long id) {
        log.info("Suppression de la ressource ID: {}", id);

        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + id));

        // Supprimer d'abord toutes les demandes associées à cette ressource
        List<DemandeRessource> demandes = demandeRessourceRepository.findByRessourceId(id);
        if (!demandes.isEmpty()) {
            demandeRessourceRepository.deleteAll(demandes);
            log.info("Supprimé {} demande(s) associée(s) à la ressource {}", demandes.size(), id);
        }

        ressourceRepository.delete(ressource);
        log.info("Ressource supprimée: {}", id);
    }

    // ========================
    // ACTIONS MÉTIER
    // ========================

    
    @Transactional
    public void changerStatutRessource(Long id, StatutRessource nouveauStatut) {
        log.info("Changement de statut de la ressource ID: {} vers {}", id, nouveauStatut);
        
        Ressource ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + id));
        
        ressource.setStatut(nouveauStatut);
        ressourceRepository.save(ressource);
        log.info("Statut de la ressource {} changé vers {}", id, nouveauStatut);
    }

    
    // ========================
    // MÉTHODES MÉTIER SPÉCIFIQUES
    // ========================

    @Transactional(readOnly = true)
    public List<RessourceDisponibleDTO> getRessourcesDisponibles(String emailEmploye) {
        log.info("Récupération des ressources disponibles pour l'employé: {}", emailEmploye);
        
        // Récupérer l'employé
        Employe employe = employeRepository.findByEmail(emailEmploye)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + emailEmploye));
        
        // Récupérer toutes les ressources avec statut = ACTIVE
        List<Ressource> ressourcesActives = ressourceRepository.findAll()
                .stream()
                .filter(r -> r.getStatut() == StatutRessource.ACTIVE)
                .collect(Collectors.toList());
        
        // Mapper vers DTO avec informations calculées
        return ressourcesActives.stream()
                .map(ressource -> {
                    RessourceDisponibleDTO dto = new RessourceDisponibleDTO();
                    dto.setId(ressource.getId());
                    dto.setNom(ressource.getNom());
                    dto.setDescription(ressource.getDescription());
                    dto.setPrix(ressource.getPrix());
                    dto.setStatut(ressource.getStatut().name());
                    // Dates d'abonnement
                    dto.setDateDebutAbonnement(ressource.getDateDebutAbonnement() != null ? 
                            ressource.getDateDebutAbonnement().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) : null);
                    dto.setDateFinAbonnement(ressource.getDateFinAbonnement() != null ? 
                            ressource.getDateFinAbonnement().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) : null);
                    dto.setEstAbonne(ressource.getDateDebutAbonnement() != null && 
                                   ressource.getDateFinAbonnement() != null);
                    
                    // Vérifier si cet employé a déjà demandé cette ressource
                    boolean dejaDemandeParMoi = demandeRessourceRepository
                            .findByRessourceAndEmploye(ressource, employe)
                            .isPresent();
                    dto.setDejaDemandeParMoi(dejaDemandeParMoi);
                    
                    // Calculer le nombre total de demandes
                    int nombreDemandes = demandeRessourceRepository.findByRessourceId(ressource.getId()).size();
                    dto.setNombreDemandes(nombreDemandes);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RessourceResponse> getAllForAdmin() {
        log.info("Récupération de toutes les ressources pour l'admin");
        
        List<Ressource> ressources = ressourceRepository.findAll();
        
        return ressources.stream()
                .map(ressource -> {
                    RessourceResponse response = mapToResponse(ressource);
                    
                    // Ajouter le nombre de demandes
                    int nombreDemandes = demandeRessourceRepository.findByRessourceId(ressource.getId()).size();
                    response.setNombreDemandes(nombreDemandes);
                    
                    return response;
                })
                .collect(Collectors.toList());
    }

    // ========================
    // CALCUL AUTOMATIQUE STATUT
    // ========================

    @Transactional
    public void updateStatutsByDate() {
        log.info("Mise à jour automatique des statuts par date de ressource");
        
        List<Ressource> ressources = ressourceRepository.findAll();
        LocalDate today = LocalDate.now();
        
        for (Ressource ressource : ressources) {
            // Vérifier si la date de fin d'abonnement est expirée
            if (ressource.getDateFinAbonnement() != null 
                && today.isAfter(ressource.getDateFinAbonnement())) {
                
                if (ressource.getStatut() != StatutRessource.NON_ACTIVE) {
                    ressource.setStatut(StatutRessource.NON_ACTIVE);
                    ressourceRepository.save(ressource);
                    log.info("Ressource {} marquée comme NON_ACTIVE (date de fin expirée le {})", 
                            ressource.getId(), ressource.getDateFinAbonnement());
                }
            }
        }
        
        log.info("Mise à jour automatique des statuts terminée");
    }

    // ========================
    // MAPPING DTO
    // ========================

    private RessourceResponse mapToResponse(Ressource ressource) {
        RessourceResponse response = new RessourceResponse();
        response.setId(ressource.getId());
        response.setNom(ressource.getNom());
        response.setDescription(ressource.getDescription());
        response.setStatut(ressource.getStatut());
        response.setPrix(ressource.getPrix());
        response.setDateDebut(ressource.getDateDebutAbonnement());
        response.setDateFin(ressource.getDateFinAbonnement());
        
                response.setDejaDemandeParMoi(false); // Sera géré dans le service employé
        
        return response;
    }
}
