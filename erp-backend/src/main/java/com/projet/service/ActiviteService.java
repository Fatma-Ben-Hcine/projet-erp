package com.projet.service;

import com.projet.dto.ActiviteRequest;
import com.projet.dto.ActiviteResponse;
import com.projet.entity.*;
import com.projet.enums.StatutActivite;
import com.projet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final ProjetRepository projetRepository;
    private final EmployeRepository employeRepository;
    private final TravaillerActiviteRepository travaillerActiviteRepository;
    private final TacheRepository tacheRepository;

    // CRUD de base
    public List<ActiviteResponse> getAllActivites() {
        log.info("Récupération de toutes les activités");
        return activiteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<ActiviteResponse> getActiviteById(Long id) {
        log.info("Récupération de l'activité avec ID: {}", id);
        return activiteRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<ActiviteResponse> getActivitesByProjetId(Long projetId) {
        log.info("Récupération des activités pour le projet ID: {}", projetId);
        return activiteRepository.findByProjetIdOrderByDateDebut(projetId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ActiviteResponse createActivite(ActiviteRequest request) {
        log.info("Création d'une nouvelle activité: {}", request.getNom());
        
        // Vérifier que le projet existe
        Projet projet = projetRepository.findById(request.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec ID: " + request.getProjetId()));

        // Créer l'activité
        Activite activite = new Activite();
        activite.setNom(request.getNom());
        activite.setDescription(request.getDescription());
        activite.setDateDebut(request.getDateDebut());
        activite.setDateFin(request.getDateFin());
        activite.setProjet(projet);

        Activite savedActivite = activiteRepository.save(activite);

        // Ajouter les employés si spécifiés
        if (request.getEmployeActivites() != null && !request.getEmployeActivites().isEmpty()) {
            for (ActiviteRequest.EmployeActiviteRequest empAct : request.getEmployeActivites()) {
                assignEmployeToActivite(savedActivite.getId(), empAct.getEmployeId(), 
                    empAct.getStatut(), empAct.getProgression());
            }
        }

        log.info("Activité créée avec succès: {}", savedActivite.getId());
        return mapToResponse(savedActivite);
    }

    public ActiviteResponse updateActivite(Long id, ActiviteRequest request) {
        log.info("Mise à jour de l'activité ID: {}", id);
        
        Activite activite = activiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + id));

        // Mettre à jour les champs
        activite.setNom(request.getNom());
        activite.setDescription(request.getDescription());
        activite.setDateDebut(request.getDateDebut());
        activite.setDateFin(request.getDateFin());

        // Mettre à jour le projet si nécessaire
        if (!activite.getProjet().getId().equals(request.getProjetId())) {
            Projet nouveauProjet = projetRepository.findById(request.getProjetId())
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé avec ID: " + request.getProjetId()));
            activite.setProjet(nouveauProjet);
        }

        Activite updatedActivite = activiteRepository.save(activite);
        log.info("Activité mise à jour avec succès: {}", updatedActivite.getId());
        return mapToResponse(updatedActivite);
    }

    public void deleteActivite(Long id) {
        log.info("Suppression de l'activité ID: {}", id);
        
        if (!activiteRepository.existsById(id)) {
            throw new RuntimeException("Activité non trouvée avec ID: " + id);
        }

        // Vérifier s'il y a des tâches associées
        List<Tache> taches = tacheRepository.findByActiviteId(id);
        if (!taches.isEmpty()) {
            throw new RuntimeException("Impossible de supprimer l'activité: elle contient des tâches");
        }

        activiteRepository.deleteById(id);
        log.info("Activité supprimée avec succès: {}", id);
    }

    // Gestion des employés
    public void assignEmployeToActivite(Long activiteId, Long employeId, StatutActivite statut, Integer progression) {
        log.info("Assignation de l'employé {} à l'activité {} avec statut {} et progression {}", 
                employeId, activiteId, statut, progression);

        Activite activite = activiteRepository.findById(activiteId)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + activiteId));

        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec ID: " + employeId));

        // Vérifier si l'assignation existe déjà
        Optional<TravaillerActivite> existing = travaillerActiviteRepository
                .findByEmployeIdAndActiviteId(employeId, activiteId);

        TravaillerActivite travaillerActivite;
        if (existing.isPresent()) {
            travaillerActivite = existing.get();
            travaillerActivite.setStatut(statut);
            travaillerActivite.setProgression(progression);
        } else {
            travaillerActivite = new TravaillerActivite(employe, activite, statut, progression);
        }

        travaillerActiviteRepository.save(travaillerActivite);
        log.info("Assignation réussie");
    }

    public void unassignEmployeFromActivite(Long activiteId, Long employeId) {
        log.info("Désassignation de l'employé {} de l'activité {}", employeId, activiteId);
        
        Optional<TravaillerActivite> existing = travaillerActiviteRepository
                .findByEmployeIdAndActiviteId(employeId, activiteId);

        if (existing.isPresent()) {
            travaillerActiviteRepository.delete(existing.get());
            log.info("Désassignation réussie");
        } else {
            throw new RuntimeException("Aucune assignation trouvée pour cet employé et cette activité");
        }
    }

    public void updateEmployeActiviteProgression(Long activiteId, Long employeId, Integer progression) {
        log.info("Mise à jour de la progression de l'employé {} pour l'activité {}: {}%", 
                employeId, activiteId, progression);

        TravaillerActivite travaillerActivite = travaillerActiviteRepository
                .findByEmployeIdAndActiviteId(employeId, activiteId)
                .orElseThrow(() -> new RuntimeException("Assignation non trouvée"));

        travaillerActivite.setProgression(progression);
        
        // Mettre à jour le statut automatiquement si progression = 100%
        if (progression >= 100) {
            travaillerActivite.setStatut(StatutActivite.TERMINE);
        } else if (progression > 0) {
            travaillerActivite.setStatut(StatutActivite.EN_COURS);
        }

        travaillerActiviteRepository.save(travaillerActivite);
        log.info("Progression mise à jour avec succès");
    }

    // Méthodes utilitaires
    private ActiviteResponse mapToResponse(Activite activite) {
        ActiviteResponse response = new ActiviteResponse();
        response.setId(activite.getId());
        response.setNom(activite.getNom());
        response.setDescription(activite.getDescription());
        response.setDateDebut(activite.getDateDebut());
        response.setDateFin(activite.getDateFin());

        // Info projet
        ActiviteResponse.ProjetInfo projetInfo = new ActiviteResponse.ProjetInfo();
        projetInfo.setId(activite.getProjet().getId());
        projetInfo.setNom(activite.getProjet().getNom());
        response.setProjet(projetInfo);

        // Info tâches
        List<ActiviteResponse.TacheInfo> taches = activite.getTaches().stream()
                .map(tache -> {
                    ActiviteResponse.TacheInfo tacheInfo = new ActiviteResponse.TacheInfo();
                    tacheInfo.setId(tache.getId());
                    tacheInfo.setNom(tache.getNom());
                    tacheInfo.setDescription(tache.getDescription());
                    tacheInfo.setDateDebut(tache.getDateDebut());
                    tacheInfo.setDateFin(tache.getDateFin());
                    // Calculer la progression moyenne des tâches
                    Double avgProgression = travaillerActiviteRepository.getAverageProgressionByActiviteId(activite.getId());
                    tacheInfo.setProgression(avgProgression != null ? avgProgression.intValue() : 0);
                    return tacheInfo;
                })
                .collect(Collectors.toList());
        response.setTaches(taches);

        // Info employés assignés
        List<ActiviteResponse.EmployeActiviteInfo> employeActivites = activite.getTravaillerActivites().stream()
                .map(ta -> {
                    ActiviteResponse.EmployeActiviteInfo info = new ActiviteResponse.EmployeActiviteInfo();
                    info.setEmployeId(ta.getEmploye().getId());
                    info.setEmployeNom(ta.getEmploye().getNom());
                    info.setEmployePrenom(ta.getEmploye().getPrenom());
                    info.setStatut(ta.getStatut());
                    info.setProgression(ta.getProgression());
                    info.setDateDebut(ta.getDateDebut());
                    info.setDateFin(ta.getDateFin());
                    return info;
                })
                .collect(Collectors.toList());
        response.setEmployeActivites(employeActivites);

        // Calculer la progression moyenne
        Double avgProgression = travaillerActiviteRepository.getAverageProgressionByActiviteId(activite.getId());
        response.setProgressionMoyenne(avgProgression != null ? avgProgression.intValue() : 0);

        // Nombre d'employés assignés
        response.setNombreEmployesAssignes(employeActivites.size());

        return response;
    }
}
