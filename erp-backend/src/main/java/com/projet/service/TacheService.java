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
import java.time.format.DateTimeFormatter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

        // Valider les dates par rapport à l'activité
        validateTaskDates(request, activite);

        // Créer la tâche
        Tache tache = new Tache();
        tache.setNom(request.getNom());
        tache.setDescription(request.getDescription());
        tache.setDateDebut(request.getDateDebut());
        tache.setDateFin(request.getDateFin());
        tache.setActivite(activite);
        tache.setEstDeposé(request.isEstDeposé());

        Tache savedTache = tacheRepository.save(tache);

        // Ajouter les employés si spécifiés
        if (request.getEmployeTaches() != null && !request.getEmployeTaches().isEmpty()) {
            for (TacheRequest.EmployeTacheRequest empTache : request.getEmployeTaches()) {
                assignEmployeToTache(savedTache.getId(), empTache.getEmployeId());
            }
        }

        log.info("Tâche créée avec succès: {}", savedTache.getId());
        return mapToResponse(savedTache);
    }

    public TacheResponse updateTache(Long id, TacheRequest request) {
        log.info("Mise à jour de la tâche ID: {}", id);
        
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID: " + id));

        // Récupérer l'activité pour la validation
        Activite activite = activiteRepository.findById(request.getActiviteId())
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + request.getActiviteId()));

        // Valider les dates par rapport à l'activité
        validateTaskDates(request, activite);

        // Mettre à jour les champs
        tache.setNom(request.getNom());
        tache.setDescription(request.getDescription());
        tache.setDateDebut(request.getDateDebut());
        tache.setDateFin(request.getDateFin());
        tache.setEstDeposé(request.isEstDeposé());

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
    public void assignEmployeToTache(Long tacheId, Long employeId) {
        log.info("Assignation de l'employé {} à la tâche {}", employeId, tacheId);

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
        } else {
            travaillerTache = new TravaillerTache(employe, tache);
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

    
    // Méthodes de statistiques
    public long getNombreTachesByActivite(Long activiteId) {
        return tacheRepository.countByActiviteId(activiteId);
    }

    public long getNombreTachesTermineesByEmploye(Long employeId) {
        return 0;
    }

    public long getNombreTachesTermineesByTache(Long tacheId) {
        return 0;
    }

    // Méthodes utilitaires
    private TacheResponse mapToResponse(Tache tache) {
        TacheResponse response = new TacheResponse();
        response.setId(tache.getId());
        response.setNom(tache.getNom());
        response.setDescription(tache.getDescription());
        response.setDateDebut(tache.getDateDebut());
        response.setDateFin(tache.getDateFin());
        response.setEstDeposé(tache.isEstDeposé());

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
                    info.setDateDebut(tt.getDateDebut());
                    info.setDateFinReelle(tt.getDateFinReelle());
                    return info;
                })
                .collect(Collectors.toList());
        response.setEmployeTaches(employeTaches);

        // Nombre d'employés assignés
        response.setNombreEmployesAssignes(employeTaches.size());

        // Nombre d'employés qui ont terminé la tâche
        response.setNombreEmployesTermines(0);

        // Calculer la progression (par défaut 0)
        response.setProgression(0);

        return response;
    }

    private void validateTaskDates(TacheRequest request, Activite activite) {
        // Validation: task.startDate >= activity.startDate
        if (request.getDateDebut().isBefore(activite.getDateDebut())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("La date de début de la tâche (%s) doit être supérieure ou égale à la date de début de l'activité (%s)",
                    request.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    activite.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }

        // Validation: task.endDate <= activity.endDate
        if (request.getDateFin() != null && request.getDateFin().isAfter(activite.getDateFin())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("La date de fin de la tâche (%s) doit être inférieure ou égale à la date de fin de l'activité (%s)",
                    request.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    activite.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }

        // Validation: task.startDate < task.endDate
        if (request.getDateFin() != null && request.getDateDebut().isAfter(request.getDateFin())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("La date de début de la tâche (%s) doit être antérieure à la date de fin (%s)",
                    request.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    request.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }
    }
}
