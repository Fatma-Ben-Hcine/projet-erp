package com.projet.service;

import com.projet.dto.ActiviteRequest;
import com.projet.dto.ActiviteResponse;
import com.projet.dto.DepotRequest;
import com.projet.dto.DepotResponse;
import com.projet.entity.*;
import com.projet.enums.StatutActivite;
import com.projet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final ProjetRepository projetRepository;
    private final EmployeRepository employeRepository;
    private final TravaillerActiviteRepository travaillerActiviteRepository;
    private final TacheRepository tacheRepository;
    private final DepotRepository depotRepository;
    private final FileUploadService fileUploadService;
    private final TacheService tacheService;

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
        try {
            List<Activite> activites = activiteRepository.findByProjetIdWithEmployesAndTaches(projetId);
            log.info("Nombre d'activités trouvées: {}", activites.size());
            return activites.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des activités pour le projet {}: {}", projetId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération des activités: " + e.getMessage(), e);
        }
    }

    public ActiviteResponse createActivite(ActiviteRequest request) {
        log.info("Création d'une nouvelle activité: {}", request.getNom());
        
        // Vérifier que le projet existe
        Projet projet = projetRepository.findById(request.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec ID: " + request.getProjetId()));

        // Valider les dates par rapport au projet
        validateActivityDates(request, projet);

        // Créer l'activité
        Activite activite = new Activite();
        activite.setNom(request.getNom());
        activite.setDescription(request.getDescription());
        activite.setDateDebut(request.getDateDebut());
        activite.setDateFin(request.getDateFin());
        activite.setProjet(projet);
        activite.setEstDeposé(request.getEstDeposé() != null ? request.getEstDeposé() : false);

        Activite savedActivite = activiteRepository.save(activite);

        // Ajouter les employés si spécifiés
        if (request.getEmployeActivites() != null && !request.getEmployeActivites().isEmpty()) {
            for (ActiviteRequest.EmployeActiviteRequest empAct : request.getEmployeActivites()) {
                assignEmployeToActivite(savedActivite.getId(), empAct.getEmployeId());
            }
        } else if (request.getEmployeIds() != null && !request.getEmployeIds().isEmpty()) {
            // Support pour employeIds (compatibilité frontend)
            for (Long employeId : request.getEmployeIds()) {
                assignEmployeToActivite(savedActivite.getId(), employeId);
            }
        }

        log.info("Activité créée avec succès: {}", savedActivite.getId());
        return mapToResponse(savedActivite);
    }

    public ActiviteResponse updateActivite(Long id, ActiviteRequest request) {
        log.info("Mise à jour de l'activité ID: {}", id);
        
        Activite activite = activiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + id));

        // Récupérer le projet pour la validation
        Projet projet = projetRepository.findById(request.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec ID: " + request.getProjetId()));

        // Valider les dates par rapport au projet
        validateActivityDates(request, projet);

        // Mettre à jour les champs
        activite.setNom(request.getNom());
        activite.setDescription(request.getDescription());
        activite.setDateDebut(request.getDateDebut());
        activite.setDateFin(request.getDateFin());
        activite.setEstDeposé(request.getEstDeposé() != null ? request.getEstDeposé() : false);

        // Mettre à jour le projet si nécessaire
        if (!activite.getProjet().getId().equals(request.getProjetId())) {
            Projet nouveauProjet = projetRepository.findById(request.getProjetId())
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé avec ID: " + request.getProjetId()));
            activite.setProjet(nouveauProjet);
        }

        // Mettre à jour les employés si spécifiés
        if (request.getEmployeActivites() != null && !request.getEmployeActivites().isEmpty()) {
            // Supprimer les anciennes assignations
            travaillerActiviteRepository.deleteByActiviteId(id);
            // Ajouter les nouvelles
            for (ActiviteRequest.EmployeActiviteRequest empAct : request.getEmployeActivites()) {
                assignEmployeToActivite(id, empAct.getEmployeId());
            }
        } else if (request.getEmployeIds() != null && !request.getEmployeIds().isEmpty()) {
            // Support pour employeIds (compatibilité frontend)
            // Supprimer les anciennes assignations
            travaillerActiviteRepository.deleteByActiviteId(id);
            // Ajouter les nouvelles
            for (Long employeId : request.getEmployeIds()) {
                assignEmployeToActivite(id, employeId);
            }
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
    public void assignEmployeToActivite(Long activiteId, Long employeId) {
        log.info("Assignation de l'employé {} à l'activité {}", 
                employeId, activiteId);

        Activite activite = activiteRepository.findById(activiteId)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec ID: " + activiteId));

        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec ID: " + employeId));

        // Vérifier si l'assignation existe déjà
        Optional<TravaillerActivite> existing = travaillerActiviteRepository
                .findByEmployeIdAndActiviteId(employeId, activiteId);

        if (existing.isEmpty()) {
            TravaillerActivite travaillerActivite = new TravaillerActivite(employe, activite);
            travaillerActiviteRepository.save(travaillerActivite);
            log.info("Assignation réussie");
        } else {
            log.info("Assignation existe déjà");
        }
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

    // Méthode supprimée : la progression est calculée dynamiquement
    // public void updateEmployeActiviteProgression(...) { ... }

    // Méthodes utilitaires
    private ActiviteResponse mapToResponse(Activite activite) {
        ActiviteResponse response = new ActiviteResponse();
        response.setId(activite.getId());
        response.setNom(activite.getNom());
        response.setDescription(activite.getDescription());
        response.setDateDebut(activite.getDateDebut());
        response.setDateFin(activite.getDateFin());
        response.setEstDeposé(activite.isEstDeposé());

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
                    tacheInfo.setEstDeposé(tache.isEstDeposé());
                    // Calculer la progression de la tâche : 100% si déposée, 0% sinon
                    tacheInfo.setProgression(tache.isEstDeposé() ? 100 : 0);
                    // Info employés assignés à la tâche
                    List<ActiviteResponse.EmployeTacheInfo> employeTaches = tache.getTravaillerTaches().stream()
                            .map(tt -> {
                                ActiviteResponse.EmployeTacheInfo info = new ActiviteResponse.EmployeTacheInfo();
                                info.setEmployeId(tt.getEmploye().getId());
                                info.setEmployeNom(tt.getEmploye().getNom());
                                info.setEmployePrenom(tt.getEmploye().getPrenom());
                                return info;
                            })
                            .collect(Collectors.toList());
                    tacheInfo.setEmployeTaches(employeTaches);
                    
                    // Info activité parente (nécessaire pour la modification de tâche)
                    ActiviteResponse.ActiviteInfo parentActiviteInfo = new ActiviteResponse.ActiviteInfo();
                    parentActiviteInfo.setId(activite.getId());
                    parentActiviteInfo.setNom(activite.getNom());
                    parentActiviteInfo.setDescription(activite.getDescription());
                    if (activite.getProjet() != null) {
                        parentActiviteInfo.setProjetId(activite.getProjet().getId());
                    }
                    tacheInfo.setActivite(parentActiviteInfo);
                    
                    // Récupérer les dépôts de la tâche
                    List<Depot> depots = depotRepository.findDepotsByTacheId(tache.getId());
                    if (depots != null && !depots.isEmpty()) {
                        List<DepotResponse> depotsResponse = depots.stream()
                                .map(depot -> {
                                    DepotResponse depotResponse = new DepotResponse();
                                    depotResponse.setId(depot.getId());
                                    depotResponse.setType(depot.getType());
                                    depotResponse.setLien(depot.getLien());
                                    depotResponse.setNomFichier(depot.getNomFichier());
                                    depotResponse.setCheminFichier(depot.getCheminFichier());
                                    depotResponse.setDateDepot(depot.getDateDepot());
                                    // IDs de relation
                                    depotResponse.setTacheId(tache.getId());
                                    depotResponse.setActiviteId(activite.getId());
                                    depotResponse.setProjetId(activite.getProjet().getId());
                                    return depotResponse;
                                })
                                .collect(Collectors.toList());
                        tacheInfo.setDepots(depotsResponse);
                    }
                    
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
                    // Progression sera calculée dynamiquement
                    info.setProgression(0);
                    return info;
                })
                .collect(Collectors.toList());
        response.setEmployeActivites(employeActivites);

        // Calculer la progression moyenne dynamiquement
        // Basée sur les tâches déposées de l'activité
        int progressionMoyenne = 0;
        if (activite.getTaches() != null && !activite.getTaches().isEmpty()) {
            long deposees = activite.getTaches().stream()
                .filter(Tache::isEstDeposé)
                .count();
            progressionMoyenne = (int) Math.round((deposees * 100.0) / activite.getTaches().size());
        }
        response.setProgressionMoyenne(progressionMoyenne);

        // Nombre d'employés assignés
        response.setNombreEmployesAssignes(employeActivites.size());

        // Mapper les dépôts d'activité uniquement (pas les dépôts de tâches)
        List<Depot> depots = depotRepository.findDepotsByActiviteIdSeulement(activite.getId());
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
                        // IDs de relation (pas de tacheId pour un dépôt d'activité)
                        depotResponse.setTacheId(null);
                        depotResponse.setActiviteId(activite.getId());
                        depotResponse.setProjetId(activite.getProjet().getId());
                        return depotResponse;
                    })
                    .collect(Collectors.toList());
            response.setDepots(depotsResponse);
        }

        return response;
    }

    private void validateActivityDates(ActiviteRequest request, Projet projet) {
        // Validation: activity.startDate >= project.startDate
        if (request.getDateDebut().isBefore(projet.getDateDebut())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("La date de début de l'activité (%s) doit être supérieure ou égale à la date de début du projet (%s)",
                    request.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    projet.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }

        // Validation: activity.endDate <= project.endDate
        if (request.getDateFin() != null && request.getDateFin().isAfter(projet.getDateLimite())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("La date de fin de l'activité (%s) doit être inférieure ou égale à la date de fin du projet (%s)",
                    request.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    projet.getDateLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }

        // Validation: activity.startDate < activity.endDate
        if (request.getDateFin() != null && request.getDateDebut().isAfter(request.getDateFin())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("La date de début de l'activité (%s) doit être antérieure à la date de fin (%s)",
                    request.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    request.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }
    }

    @Transactional
    public ActiviteResponse deposerActivite(Long id, DepotRequest depotRequest, MultipartFile file) throws IOException {
        log.info("Dépôt de l'activité {}", id);
        Activite activite = activiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec l'id: " + id));
        activite.setEstDeposé(true);
        activite = activiteRepository.save(activite);

        // Chercher un dépôt existant pour cette activité (tache IS NULL)
        List<Depot> existingDepots = depotRepository.findDepotsByActiviteIdSeulement(id);
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
        depot.setActivite(activite);
        depot.setTache(null);  // ← IMPORTANT : pas de tâche pour un dépôt d'activité
        // Récupérer le projet depuis l'activité
        if (activite.getProjet() != null) {
            depot.setProjet(activite.getProjet());
        }
        depotRepository.save(depot);

        // Rafraîchir l'activité depuis la base pour charger les dépôts associés
        activite = activiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée avec l'id: " + id));

        log.info("Activité {} déposée avec dépôt {}", id, depot.getId());
        return mapToResponse(activite);
    }

    public boolean hasDepot(Long activiteId) {
        List<Depot> depots = depotRepository.findDepotsByActiviteIdSeulement(activiteId);
        return !depots.isEmpty();
    }

    public List<Map<String, Object>> getEmployesByActiviteId(Long activiteId) {
        log.info("Récupération des employés pour l'activité ID: {}", activiteId);
        List<TravaillerActivite> travaillerActivites = travaillerActiviteRepository.findByActiviteIdWithQuery(activiteId);
        log.info("Nombre d'entrées travailler_activite trouvées pour l'activité {}: {}", activiteId, travaillerActivites.size());
        
        return travaillerActivites.stream()
                .map(ta -> {
                    Map<String, Object> employeInfo = new java.util.HashMap<>();
                    employeInfo.put("id", ta.getEmploye().getId());
                    employeInfo.put("nom", ta.getEmploye().getNom());
                    employeInfo.put("prenom", ta.getEmploye().getPrenom());
                    // Progression sera calculée dynamiquement
                    employeInfo.put("progression", 0);
                    return employeInfo;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean areAllTachesDeposees(Long activiteId) {
        Activite activite = activiteRepository.findById(activiteId)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
        if (activite.getTaches() == null || activite.getTaches().isEmpty()) {
            return false;
        }
        return activite.getTaches().stream().allMatch(Tache::isEstDeposé);
    }
}
