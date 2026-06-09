package com.projet.service;

import com.projet.dto.DemandeRessourceRequest;
import com.projet.dto.DemandeRessourceResponse;
import com.projet.entity.DemandeRessource;
import com.projet.entity.Employe;
import com.projet.entity.Ressource;
import com.projet.enums.StatutRessource;
import com.projet.enums.StatutDemande;
import com.projet.repository.DemandeRessourceRepository;
import com.projet.repository.EmployeRepository;
import com.projet.repository.RessourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeRessourceService {

    private final DemandeRessourceRepository demandeRepository;
    private final RessourceRepository ressourceRepository;
    private final EmployeRepository employeRepository;

    /**
     * Créer une demande de ressource par un employé
     */
    @Transactional
    public DemandeRessourceResponse createDemande(DemandeRessourceRequest request, Long employeId) {
        log.info("Création d'une demande de ressource - employeId: {}, ressourceId: {}",
                employeId, request.getRessourceId());

        // Récupérer la ressource
        Ressource ressource = ressourceRepository.findById(request.getRessourceId())
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'id: " + request.getRessourceId()));

        // Récupérer l'employé
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'id: " + employeId));

        // Vérifications
        if (ressource.getStatut() != StatutRessource.ACTIVE) {
            throw new RuntimeException("Cette ressource n'est pas disponible (statut: " + ressource.getStatut() + ")");
        }

        
        // Créer la demande
        DemandeRessource demande = new DemandeRessource();
        demande.setRessource(ressource);
        demande.setEmploye(employe);
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatutDemande(StatutDemande.EN_ATTENTE);

        // Sauvegarder la demande
        DemandeRessource savedDemande = demandeRepository.save(demande);

        log.info("Demande de ressource créée avec succès - ID: {}, Employé: {}, Ressource: {}", 
                savedDemande.getId(), employe.getEmail(), ressource.getNom());

        return mapToResponse(savedDemande);
    }

    /**
     * Récupérer les demandes d'un employé spécifique
     */
    @Transactional(readOnly = true)
    public List<DemandeRessourceResponse> getDemandesByEmploye(Long employeId) {
        log.debug("Récupération des demandes pour l'employé ID: {}", employeId);

        List<DemandeRessource> demandes = demandeRepository.findByEmployeId(employeId);

        return demandes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer toutes les demandes (pour l'admin)
     */
    @Transactional(readOnly = true)
    public List<DemandeRessourceResponse> getAllDemandes() {
        log.debug("Récupération de toutes les demandes de ressources");

        List<DemandeRessource> demandes = demandeRepository.findAll();

        return demandes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les demandes en attente
     */
    @Transactional(readOnly = true)
    public List<DemandeRessourceResponse> getDemandesEnAttente() {
        log.debug("Récupération des demandes en attente");

        List<DemandeRessource> demandes = demandeRepository.findByStatutDemande(StatutDemande.EN_ATTENTE);

        return demandes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approuver une demande (admin)
     */
    @Transactional
    public DemandeRessourceResponse approuverDemande(Long demandeId) {
        log.info("Approbation de la demande {}", demandeId);

        DemandeRessource demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + demandeId));

        demande.setStatutDemande(StatutDemande.APPROUVEE);
        DemandeRessource updated = demandeRepository.save(demande);

        log.info("Demande approuvée: {}", demandeId);
        return mapToResponse(updated);
    }

    /**
     * Annuler une demande (employé)
     */
    @Transactional
    public void annulerDemande(Long demandeId, Long employeId) {
        log.info("Annulation de la demande {} par l'employé {}", demandeId, employeId);

        DemandeRessource demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + demandeId));

        // Vérifier que l'employé est bien le propriétaire de la demande
        if (!demande.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Vous ne pouvez annuler que vos propres demandes");
        }

        demande.setStatutDemande(StatutDemande.ANNULEE);
        demandeRepository.save(demande);

        // Libérer la ressource
        Ressource ressource = demande.getRessource();
        ressourceRepository.save(ressource);

        log.info("Demande annulée et ressource libérée: {}", demandeId);
    }

    
    // ========================
    // MAPPING DTO
    // ========================

    private DemandeRessourceResponse mapToResponse(DemandeRessource demande) {
        DemandeRessourceResponse response = new DemandeRessourceResponse();
        response.setId(demande.getId());
        response.setDateDemande(demande.getDateDemande());
        response.setStatutDemande(demande.getStatutDemande());

        // Informations de la ressource
        if (demande.getRessource() != null) {
            DemandeRessourceResponse.RessourceInfo ressourceInfo = new DemandeRessourceResponse.RessourceInfo();
            ressourceInfo.setId(demande.getRessource().getId());
            ressourceInfo.setNom(demande.getRessource().getNom());
            ressourceInfo.setDescription(demande.getRessource().getDescription());
            ressourceInfo.setPrix(demande.getRessource().getPrix());
            ressourceInfo.setDateDebut(demande.getRessource().getDateDebutAbonnement());
            ressourceInfo.setDateFin(demande.getRessource().getDateFinAbonnement());
            response.setRessource(ressourceInfo);
        }

        // Informations de l'employé
        if (demande.getEmploye() != null) {
            DemandeRessourceResponse.EmployeInfo employeInfo = new DemandeRessourceResponse.EmployeInfo();
            employeInfo.setId(demande.getEmploye().getId());
            employeInfo.setNom(demande.getEmploye().getNom());
            employeInfo.setPrenom(demande.getEmploye().getPrenom());
            response.setEmploye(employeInfo);
        }

        return response;
    }

    /**
     * Vérifier si un employé a déjà demandé une ressource
     */
    @Transactional(readOnly = true)
    public boolean checkIfAlreadyRequested(Long ressourceId, Long employeId) {
        log.debug("Vérification si l'employé {} a déjà demandé la ressource {}", employeId, ressourceId);
        
        // Récupérer la ressource
        Ressource ressource = ressourceRepository.findById(ressourceId)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée: " + ressourceId));
        
        // Récupérer l'employé
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + employeId));
        
        // Vérifier si une demande existe pour cette ressource et cet employé
        return demandeRepository.findByRessourceAndEmploye(ressource, employe).isPresent();
    }
}
