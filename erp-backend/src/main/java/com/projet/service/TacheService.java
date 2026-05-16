package com.projet.service;

import com.projet.dto.TacheRequest;
import com.projet.dto.TacheResponse;
import com.projet.dto.DepotRequest;
import com.projet.entity.*;
import com.projet.enums.StatutTache;
import com.projet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
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
    private final DepotRepository depotRepository;
    private final FileUploadService fileUploadService;

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
        tache.setEstDeposé(request.getEstDeposé() != null ? request.getEstDeposé() : false);

        Tache savedTache = tacheRepository.save(tache);

        // Ajouter les employés si spécifiés
        if (request.getEmployeTaches() != null && !request.getEmployeTaches().isEmpty()) {
            for (TacheRequest.EmployeTacheRequest empTache : request.getEmployeTaches()) {
                assignEmployeToTache(savedTache.getId(), empTache.getEmployeId());
            }
        } else if (request.getEmployeIds() != null && !request.getEmployeIds().isEmpty()) {
            // Support pour employeIds (compatibilité frontend)
            for (Long employeId : request.getEmployeIds()) {
                assignEmployeToTache(savedTache.getId(), employeId);
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
        tache.setEstDeposé(request.getEstDeposé() != null ? request.getEstDeposé() : false);

        // Mettre à jour l'activité si nécessaire
        if (!tache.getActivite().getId().equals(request.getActiviteId())) {
            Activite nouvelleActivite = activiteRepository.findById(request.getActiviteId())
                    .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + request.getActiviteId()));
            tache.setActivite(nouvelleActivite);
        }

        // Mettre à jour les employés si spécifiés
        if (request.getEmployeTaches() != null && !request.getEmployeTaches().isEmpty()) {
            // Supprimer les anciennes assignations
            travaillerTacheRepository.deleteByTacheId(id);
            // Ajouter les nouvelles
            for (TacheRequest.EmployeTacheRequest empTache : request.getEmployeTaches()) {
                assignEmployeToTache(id, empTache.getEmployeId());
            }
        } else if (request.getEmployeIds() != null && !request.getEmployeIds().isEmpty()) {
            // Support pour employeIds (compatibilité frontend)
            // Supprimer les anciennes assignations
            travaillerTacheRepository.deleteByTacheId(id);
            // Ajouter les nouvelles
            for (Long employeId : request.getEmployeIds()) {
                assignEmployeToTache(id, employeId);
            }
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
        if (tache.getActivite() != null) {
            TacheResponse.ActiviteInfo activiteInfo = new TacheResponse.ActiviteInfo();
            activiteInfo.setId(tache.getActivite().getId());
            activiteInfo.setNom(tache.getActivite().getNom());
            activiteInfo.setDescription(tache.getActivite().getDescription());
            // Ajout du projetId pour faciliter la vérification du chef de projet
            if (tache.getActivite().getProjet() != null) {
                activiteInfo.setProjetId(tache.getActivite().getProjet().getId());
            }
            response.setActivite(activiteInfo);
        } else {
            log.warn("Tâche {} sans activité associée", tache.getId());
        }

        // Info employés assignés
        List<TacheResponse.EmployeTacheInfo> employeTaches = tache.getTravaillerTaches().stream()
                .map(tt -> {
                    TacheResponse.EmployeTacheInfo info = new TacheResponse.EmployeTacheInfo();
                    info.setEmployeId(tt.getEmploye().getId());
                    info.setEmployeNom(tt.getEmploye().getNom());
                    info.setEmployePrenom(tt.getEmploye().getPrenom());
                    // Les dates viennent de la tâche, pas de l'association
                    info.setDateDebut(tache.getDateDebut());
                    info.setDateFinReelle(tache.getDateFin());
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

        // Mapper les dépôts
        List<Depot> depots = depotRepository.findByTacheId(tache.getId());
        if (depots != null && !depots.isEmpty()) {
            List<com.projet.dto.DepotResponse> depotsResponse = depots.stream()
                    .map(depot -> {
                        com.projet.dto.DepotResponse depotResponse = new com.projet.dto.DepotResponse();
                        depotResponse.setId(depot.getId());
                        depotResponse.setType(depot.getType());
                        depotResponse.setLien(depot.getLien());
                        depotResponse.setNomFichier(depot.getNomFichier());
                        depotResponse.setCheminFichier(depot.getCheminFichier());
                        depotResponse.setDateDepot(depot.getDateDepot());
                        // IDs de relation
                        depotResponse.setTacheId(tache.getId());
                        depotResponse.setActiviteId(tache.getActivite().getId());
                        depotResponse.setProjetId(tache.getActivite().getProjet().getId());
                        return depotResponse;
                    })
                    .collect(Collectors.toList());
            response.setDepots(depotsResponse);
        }

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

    @Transactional
    public TacheResponse deposerTache(Long id, DepotRequest depotRequest, MultipartFile file) throws IOException {
        log.info("Dépôt de la tâche {}", id);
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + id));
        tache.setEstDeposé(true);
        tache = tacheRepository.save(tache);

        // Chercher un dépôt existant pour cette tâche
        List<Depot> existingDepots = depotRepository.findByTacheId(id);
        Depot depot = existingDepots.isEmpty() ? new Depot() : existingDepots.get(0);

        depot.setType(depotRequest.getType());
        depot.setLien(depotRequest.getLien());
        depot.setNomFichier(depotRequest.getNomFichier());

        // Si c'est un fichier, le stocker physiquement
        if ("fichier".equals(depotRequest.getType()) && file != null && !file.isEmpty()) {
            String filePath = fileUploadService.uploadDepotFile(file);
            depot.setCheminFichier(filePath);
        } else {
            depot.setCheminFichier(depotRequest.getCheminFichier());
        }

        depot.setDateDepot(java.time.LocalDateTime.now());
        depot.setTache(tache);
        // Récupérer l'activité et le projet depuis la tâche
        if (tache.getActivite() != null) {
            depot.setActivite(tache.getActivite());
            if (tache.getActivite().getProjet() != null) {
                depot.setProjet(tache.getActivite().getProjet());
            }
        }
        depotRepository.save(depot);

        // Rafraîchir la tâche depuis la base pour charger les dépôts associés
        tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + id));

        log.info("Tâche {} déposée avec dépôt {}", id, depot.getId());
        return mapToResponse(tache);
    }

    public boolean hasDepot(Long tacheId) {
        List<Depot> depots = depotRepository.findByTacheId(tacheId);
        return !depots.isEmpty();
    }
}
