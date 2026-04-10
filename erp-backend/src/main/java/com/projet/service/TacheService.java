package com.projet.service;

import com.projet.dto.TacheRequest;
import com.projet.dto.TacheResponse;
import com.projet.entity.*;
import com.projet.enums.StatutTache;
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
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ActiviteRepository activiteRepository;
    private final EmployeRepository employeRepository;
    private final TravaillerTacheRepository travaillerTacheRepository;

    // CRUD de base
    public List<TacheResponse> getAllTaches() {
        log.info("Récupération de toutes les tâches");
        return tacheRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<TacheResponse> getTacheById(Long id) {
        log.info("Récupération de la tâche avec ID: {}", id);
        return tacheRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<TacheResponse> getTachesByActiviteId(Long activiteId) {
        log.info("Récupération des tâches pour l'activité ID: {}", activiteId);
        return tacheRepository.findByActiviteIdOrderByDateDebut(activiteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TacheResponse> getTachesByEmployeId(Long employeId) {
        log.info("Récupération des tâches pour l'employé ID: {}", employeId);
        return tacheRepository.findByEmployeId(employeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TacheResponse createTache(TacheRequest request) {
        log.info("Création d'une nouvelle tâche: {}", request.getNom());
        
        // Vérifier que l'activité existe
        Activite activite = activiteRepository.findById(request.getActiviteId())
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + request.getActiviteId()));

        // Créer la tâche
        Tache tache = new Tache();
        tache.setNom(request.getNom());
        tache.setDescription(request.getDescription());
        tache.setDateDebut(request.getDateDebut());
        tache.setDateFin(request.getDateFin());
        tache.setActivite(activite);

        Tache savedTache = tacheRepository.save(tache);

        // Ajouter les employés si spécifiés
        if (request.getEmployeTaches() != null && !request.getEmployeTaches().isEmpty()) {
            for (TacheRequest.EmployeTacheRequest empTache : request.getEmployeTaches()) {
                assignEmployeToTache(savedTache.getId(), empTache.getEmployeId(), empTache.getStatut());
            }
        }

        log.info("Tâche créée avec succès: {}", savedTache.getId());
        return mapToResponse(savedTache);
    }

    public TacheResponse updateTache(Long id, TacheRequest request) {
        log.info("Mise à jour de la tâche ID: {}", id);
        
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID: " + id));

        // Mettre à jour les champs
        tache.setNom(request.getNom());
        tache.setDescription(request.getDescription());
        tache.setDateDebut(request.getDateDebut());
        tache.setDateFin(request.getDateFin());

        // Mettre à jour l'activité si nécessaire
        if (!tache.getActivite().getId().equals(request.getActiviteId())) {
            Activite nouvelleActivite = activiteRepository.findById(request.getActiviteId())
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + request.getActiviteId()));
            tache.setActivite(nouvelleActivite);
        }

        Tache updatedTache = tacheRepository.save(tache);
        log.info("Tâche mise à jour avec succès: {}", updatedTache.getId());
        return mapToResponse(updatedTache);
    }

    public void deleteTache(Long id) {
        log.info("Suppression de la tâche ID: {}", id);
        
        if (!tacheRepository.existsById(id)) {
            throw new RuntimeException("Tâche non trouvée avec ID: " + id);
        }

        tacheRepository.deleteById(id);
        log.info("Tâche supprimée avec succès: {}", id);
    }

    // Gestion des employés
    public void assignEmployeToTache(Long tacheId, Long employeId, StatutTache statut) {
        log.info("Assignation de l'employé {} à la tâche {} avec statut {}", employeId, tacheId, statut);

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID: " + tacheId));

        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec ID: " + employeId));

        // Vérifier si l'assignation existe déjà
        Optional<TravaillerTache> existing = travaillerTacheRepository
                .findByEmployeIdAndTacheId(employeId, tacheId);

        TravaillerTache travaillerTache;
        if (existing.isPresent()) {
            travaillerTache = existing.get();
            travaillerTache.setStatut(statut);
        } else {
            travaillerTache = new TravaillerTache(employe, tache, statut);
        }

        travaillerTacheRepository.save(travaillerTache);
        log.info("Assignation réussie");
    }

    public void unassignEmployeFromTache(Long tacheId, Long employeId) {
        log.info("Désassignation de l'employé {} de la tâche {}", employeId, tacheId);
        
        Optional<TravaillerTache> existing = travaillerTacheRepository
                .findByEmployeIdAndTacheId(employeId, tacheId);

        if (existing.isPresent()) {
            travaillerTacheRepository.delete(existing.get());
            log.info("Désassignation réussie");
        } else {
            throw new RuntimeException("Aucune assignation trouvée pour cet employé et cette tâche");
        }
    }

    public void updateEmployeTacheStatut(Long tacheId, Long employeId, StatutTache statut) {
        log.info("Mise à jour du statut de l'employé {} pour la tâche {}: {}", 
                employeId, tacheId, statut);

        TravaillerTache travaillerTache = travaillerTacheRepository
                .findByEmployeIdAndTacheId(employeId, tacheId)
                .orElseThrow(() -> new RuntimeException("Assignation non trouvée"));

        travaillerTache.setStatut(statut);
        
        // Mettre à jour la date de fin réelle si la tâche est terminée
        if (statut == StatutTache.TERMINE) {
            travaillerTache.setDateFinReelle(java.time.LocalDate.now());
        }

        travaillerTacheRepository.save(travaillerTache);
        log.info("Statut mis à jour avec succès");
    }

    // Méthodes de statistiques
    public long getNombreTachesByActivite(Long activiteId) {
        return tacheRepository.countByActiviteId(activiteId);
    }

    public long getNombreTachesTermineesByEmploye(Long employeId) {
        return travaillerTacheRepository.countCompletedTasksByEmployeId(employeId);
    }

    public long getNombreTachesTermineesByTache(Long tacheId) {
        return travaillerTacheRepository.countCompletedTasksByTacheId(tacheId);
    }

    // Méthodes utilitaires
    private TacheResponse mapToResponse(Tache tache) {
        TacheResponse response = new TacheResponse();
        response.setId(tache.getId());
        response.setNom(tache.getNom());
        response.setDescription(tache.getDescription());
        response.setDateDebut(tache.getDateDebut());
        response.setDateFin(tache.getDateFin());

        // Info activité
        TacheResponse.ActiviteInfo activiteInfo = new TacheResponse.ActiviteInfo();
        activiteInfo.setId(tache.getActivite().getId());
        activiteInfo.setNom(tache.getActivite().getNom());
        activiteInfo.setDescription(tache.getActivite().getDescription());
        response.setActivite(activiteInfo);

        // Info employés assignés
        List<TacheResponse.EmployeTacheInfo> employeTaches = tache.getTravaillerTaches().stream()
                .map(tt -> {
                    TacheResponse.EmployeTacheInfo info = new TacheResponse.EmployeTacheInfo();
                    info.setEmployeId(tt.getEmploye().getId());
                    info.setEmployeNom(tt.getEmploye().getNom());
                    info.setEmployePrenom(tt.getEmploye().getPrenom());
                    info.setStatut(tt.getStatut());
                    info.setDateDebut(tt.getDateDebut());
                    info.setDateFinReelle(tt.getDateFinReelle());
                    return info;
                })
                .collect(Collectors.toList());
        response.setEmployeTaches(employeTaches);

        // Nombre d'employés assignés
        response.setNombreEmployesAssignes(employeTaches.size());

        // Nombre d'employés qui ont terminé la tâche
        long nombreTermines = employeTaches.stream()
                .filter(info -> info.getStatut() == StatutTache.TERMINE)
                .count();
        response.setNombreEmployesTermines((int) nombreTermines);

        // Calculer la progression (basé sur le nombre d'employés qui ont terminé)
        if (response.getNombreEmployesAssignes() > 0) {
            response.setProgression((int) ((nombreTermines * 100) / response.getNombreEmployesAssignes()));
        } else {
            response.setProgression(0);
        }

        return response;
    }
}
