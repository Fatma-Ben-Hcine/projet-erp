package com.projet.service;

import com.projet.dto.DemandeRessourceRequest;
import com.projet.dto.DemandeRessourceResponse;
import com.projet.entity.DemandeRessource;
import com.projet.entity.Employe;
import com.projet.entity.Ressource;
import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import com.projet.repository.DemandeRessourceRepository;
import com.projet.repository.EmployeRepository;
import com.projet.repository.RessourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Vérifier si une demande existe déjà pour cet employé et cette ressource
        demandeRepository.findByRessourceAndEmploye(ressource, employe).ifPresent(d -> {
            throw new RuntimeException("Vous avez déjà demandé cette ressource");
        });

        // Créer la demande
        DemandeRessource demande = new DemandeRessource();
        demande.setRessource(ressource);
        demande.setEmploye(employe);
        demande.setDateDemande(LocalDate.now());
        demande.setEstTraitee(false);

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
     * Récupérer les demandes non traitées
     */
    @Transactional(readOnly = true)
    public List<DemandeRessourceResponse> getDemandesNonTraitees() {
        log.debug("Récupération des demandes non traitées");

        List<DemandeRessource> demandes = demandeRepository.findByEstTraiteeFalse();

        return demandes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Marquer une demande comme traitée
     */
    @Transactional
    public DemandeRessourceResponse marquerDemandeTraitee(Long demandeId) {
        log.info("Marquage de la demande {} comme traitée", demandeId);

        DemandeRessource demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + demandeId));

        demande.setEstTraitee(true);
        DemandeRessource updated = demandeRepository.save(demande);

        return mapToResponse(updated);
    }

    /**
     * Supprimer une demande
     */
    @Transactional
    public void deleteDemande(Long demandeId) {
        log.info("Suppression de la demande ID: {}", demandeId);

        DemandeRessource demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée avec l'id: " + demandeId));

        // Remettre la ressource en situation "non demandé"
        Ressource ressource = demande.getRessource();
        ressource.setSituation(SituationRessource.NON_DEMANDE);
        ressourceRepository.save(ressource);

        demandeRepository.delete(demande);
        log.info("Demande supprimée: {}", demandeId);
    }

    // ========================
    // MAPPING DTO
    // ========================

    private DemandeRessourceResponse mapToResponse(DemandeRessource demande) {
        DemandeRessourceResponse response = new DemandeRessourceResponse();
        response.setId(demande.getId());
        response.setDateDemande(demande.getDateDemande());
        response.setEstTraitee(demande.isEstTraitee());

        // Informations de la ressource
        if (demande.getRessource() != null) {
            DemandeRessourceResponse.RessourceInfo ressourceInfo = new DemandeRessourceResponse.RessourceInfo();
            ressourceInfo.setId(demande.getRessource().getId());
            ressourceInfo.setNom(demande.getRessource().getNom());
            ressourceInfo.setDescription(demande.getRessource().getDescription());
            ressourceInfo.setPrix(demande.getRessource().getPrix().doubleValue());
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
}
