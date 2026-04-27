package com.projet.service;

import com.projet.dto.ProjetRequest;
import com.projet.dto.ProjetResponse;
import com.projet.dto.DepotRequest;
import com.projet.dto.DepotResponse;
import com.projet.entity.Projet;
import com.projet.entity.TravaillerProjet;
import com.projet.entity.Employe;
import com.projet.entity.Client;
import com.projet.entity.Utilisateur;
import com.projet.entity.Activite;
import com.projet.entity.Tache;
import com.projet.entity.TravaillerActivite;
import com.projet.entity.TravaillerTache;
import com.projet.entity.Depot;
import com.projet.enums.Role;
import com.projet.enums.StatutProjet;
import com.projet.repository.ProjetRepository;
import com.projet.repository.TravaillerProjetRepository;
import com.projet.repository.ClientRepository;
import com.projet.repository.UtilisateurRepository;
import com.projet.repository.ActiviteRepository;
import com.projet.repository.TacheRepository;
import com.projet.repository.TravaillerActiviteRepository;
import com.projet.repository.TravaillerTacheRepository;
import com.projet.repository.DepotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final TravaillerProjetRepository travaillerProjetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final ActiviteRepository activiteRepository;
    private final TacheRepository tacheRepository;
    private final TravaillerActiviteRepository travaillerActiviteRepository;
    private final TravaillerTacheRepository travaillerTacheRepository;
    private final DepotRepository depotRepository;
    private final FileUploadService fileUploadService;
    private final ProjetProgressionService projetProgressionService;

    public List<ProjetResponse> getAllProjets() {
        return projetRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjetResponse getProjetById(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));
        return mapToResponse(projet);
    }

    public List<ProjetResponse> searchProjets(String keyword) {
        return projetRepository.findByNomContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjetResponse createProjet(ProjetRequest request) {
        try {
            log.info("Création projet - Request: nom={}, chefDeProjetId={}, employeIds={}", 
                    request.getNom(), request.getChefDeProjetId(), request.getEmployeIds());
            
            validateDates(request.getDateDebut(), request.getDateLimite());
            
            Projet projet = new Projet();
            projet.setNom(request.getNom());
            projet.setDescription(request.getDescription());
            projet.setBudget(request.getBudget());
            projet.setDateDebut(request.getDateDebut());
            projet.setDateLimite(request.getDateLimite());
            projet.setProgression(request.getProgression());
            projet.setEstDeposé(request.isEstDeposé());

            // Gérer le client
            if (request.getClientId() != null) {
                log.info("Recherche client avec ID: {}", request.getClientId());
                Client client = clientRepository.findById(request.getClientId())
                        .orElseThrow(() -> new RuntimeException("Client non trouvé: " + request.getClientId()));
                projet.setClient(client);
                log.info("Client trouvé: {}", client.getNom());
            }

            // Gérer le chef de projet
            if (request.getChefDeProjetId() != null) {
                log.info("Recherche chef de projet avec ID: {}", request.getChefDeProjetId());

                Employe chefProjet = utilisateurRepository.findById(request.getChefDeProjetId())
                    .filter(Employe.class::isInstance)
                    .map(Employe.class::cast)
                    .orElseThrow(() -> new RuntimeException(
                        "Employé non trouvé avec id: " + request.getChefDeProjetId()
                    ));

                if (request.getEmployeIds() == null || !request.getEmployeIds().contains(request.getChefDeProjetId())) {
                    throw new IllegalArgumentException(
                        "Le chef de projet doit être parmi les employés assignés au projet"
                    );
                }

                projet.setChefProjet(chefProjet);
                log.info("Chef de projet assigné: {} {}", chefProjet.getNom(), chefProjet.getPrenom());
            }

            Projet saved = projetRepository.save(projet);
            log.info("Projet sauvegardé avec ID: {}", saved.getId());

            // Gérer les employés via TravaillerProjet
            if (request.getEmployeIds() != null && !request.getEmployeIds().isEmpty()) {
                log.info("Assignation de {} employés au projet {}", request.getEmployeIds().size(), saved.getId());
                List<TravaillerProjet> travaillerProjets = request.getEmployeIds()
                        .stream()
                        .map(employeId -> {
                            log.info("Recherche employé avec ID: {}", employeId);
                            Utilisateur utilisateur = utilisateurRepository.findById(employeId)
                            .orElseThrow(() -> new RuntimeException(
                                "Employé non trouvé avec id: " + employeId
                            ));
                    
                    // Vérifier que l'utilisateur est bien un Employe via le discriminator JPA
                    // Le discriminator JPA est géré automatiquement par Hibernate, on vérifie juste si c'est une instance d'Employe
                    if (!(utilisateur instanceof Employe)) {
                        throw new RuntimeException(
                            "L'utilisateur avec id=" + employeId + " n'est pas un employé"
                        );
                    }
                    
                    Employe employe = (Employe) utilisateur;
                            TravaillerProjet tp = new TravaillerProjet();
                            tp.setProjet(saved);
                            tp.setEmploye(employe);
                            return tp;
                        })
                        .collect(Collectors.toList());
                
                travaillerProjetRepository.saveAll(travaillerProjets);
                log.info("{} employés assignés au projet {}", travaillerProjets.size(), saved.getId());
            }

            log.info("Projet créé avec succès: {}", saved.getNom());
            return mapToResponse(saved);
            
        } catch (Exception e) {
            log.error("Erreur création projet: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public ProjetResponse updateProjet(Long id, ProjetRequest request) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));

        validateDates(request.getDateDebut(), request.getDateLimite());

        projet.setNom(request.getNom());
        projet.setDescription(request.getDescription());
        projet.setBudget(request.getBudget());
        projet.setDateDebut(request.getDateDebut());
        projet.setDateLimite(request.getDateLimite());
        projet.setProgression(request.getProgression());
        projet.setEstDeposé(request.isEstDeposé());

        // Gérer le client
        if (request.getClientId() != null) {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé: " + request.getClientId()));
            projet.setClient(client);
        }

        // Gérer le chef de projet
        if (request.getChefDeProjetId() != null) {
            Utilisateur utilisateur = utilisateurRepository.findById(request.getChefDeProjetId())
                .orElseThrow(() -> new RuntimeException(
                    "Utilisateur non trouvé avec id: " + request.getChefDeProjetId()
                ));
            
            // Permettre à n'importe quel utilisateur (admin ou employé) d'être chef de projet
            // On vérifie juste que l'utilisateur est actif
            if (!utilisateur.isActif()) {
                throw new RuntimeException(
                    "L'utilisateur avec id=" + request.getChefDeProjetId() + " n'est pas actif"
                );
            }
            
            Employe chefProjet = (Employe) utilisateur;

            if (request.getEmployeIds() == null || !request.getEmployeIds().contains(request.getChefDeProjetId())) {
                throw new IllegalArgumentException(
                    "Le chef de projet doit être parmi les employés assignés au projet"
                );
            }

            projet.setChefProjet(chefProjet);
        }

        // Gérer les employés - supprimer les anciennes relations et créer les nouvelles
        if (request.getEmployeIds() != null) {
            log.info("Mise à jour des employés pour le projet {}: {} employés", id, request.getEmployeIds().size());
            
            // Récupérer et supprimer manuellement toutes les relations existantes
            List<TravaillerProjet> anciennesRelations = travaillerProjetRepository.findByProjetId(id);
            if (!anciennesRelations.isEmpty()) {
                log.info("Suppression de {} anciennes relations employé-projet pour le projet {}", anciennesRelations.size(), id);
                for (TravaillerProjet relation : anciennesRelations) {
                    log.debug("Suppression de la relation: projet_id={}, employe_id={}", 
                        relation.getProjet().getId(), relation.getEmploye().getId());
                    travaillerProjetRepository.delete(relation);
                }
                // Forcer la synchronisation avec la base de données
                travaillerProjetRepository.flush();
                log.info("Toutes les anciennes relations ont été supprimées avec succès");
            }
            
            // Créer les nouvelles relations
            if (!request.getEmployeIds().isEmpty()) {
                List<TravaillerProjet> nouvellesRelations = new ArrayList<>();
                for (Long employeId : request.getEmployeIds()) {
                    log.info("Recherche employé avec ID: {}", employeId);
                    Utilisateur utilisateur = utilisateurRepository.findById(employeId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Employé non trouvé avec id: " + employeId
                            ));
                    
                    // Vérifier que l'utilisateur est bien un Employe
                    if (!(utilisateur instanceof Employe)) {
                        throw new RuntimeException(
                                "L'utilisateur avec id=" + employeId + " n'est pas un employé"
                        );
                    }
                    
                    Employe employe = (Employe) utilisateur;
                    TravaillerProjet tp = new TravaillerProjet();
                    tp.setProjet(projet);
                    tp.setEmploye(employe);
                    nouvellesRelations.add(tp);
                    log.debug("Création de la relation: projet_id={}, employe_id={}", id, employeId);
                }
                
                travaillerProjetRepository.saveAll(nouvellesRelations);
                log.info("Créé {} nouvelles relations employé-projet", nouvellesRelations.size());
            } else {
                log.info("Aucun employé à assigner au projet {}", id);
            }
        }

        Projet updated = projetRepository.save(projet);
        log.info("Projet mis à jour: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProjet(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));
        
        log.info("Début de la suppression complète du projet {} et de toutes ses relations", id);
        
        try {
            // 1. Récupérer toutes les activités du projet
            List<Activite> activites = activiteRepository.findByProjetId(id);
            log.info("Trouvé {} activités pour le projet {}", activites.size(), id);
            
            for (Activite activite : activites) {
                // 2. Récupérer toutes les tâches de chaque activité
                List<Tache> taches = tacheRepository.findByActiviteId(activite.getId());
                log.info("Activité {}: {} tâches à supprimer", activite.getId(), taches.size());
                
                for (Tache tache : taches) {
                    // 3. Supprimer les assignements de tâches (travailler_tache)
                    List<TravaillerTache> travaillerTaches = travaillerTacheRepository.findByTacheId(tache.getId());
                    if (!travaillerTaches.isEmpty()) {
                        log.debug("Suppression de {} assignements pour la tâche {}", travaillerTaches.size(), tache.getId());
                        travaillerTacheRepository.deleteAll(travaillerTaches);
                    }
                }
                
                // 4. Supprimer les tâches
                if (!taches.isEmpty()) {
                    log.debug("Suppression de {} tâches pour l'activité {}", taches.size(), activite.getId());
                    tacheRepository.deleteByActiviteId(activite.getId());
                }
                
                // 5. Supprimer les assignements d'activités (travailler_activite)
                List<TravaillerActivite> travaillerActivites = travaillerActiviteRepository.findByActiviteId(activite.getId());
                if (!travaillerActivites.isEmpty()) {
                    log.debug("Suppression de {} assignements pour l'activité {}", travaillerActivites.size(), activite.getId());
                    travaillerActiviteRepository.deleteByActiviteId(activite.getId());
                }
            }
            
            // 6. Supprimer les activités
            if (!activites.isEmpty()) {
                log.info("Suppression de {} activités pour le projet {}", activites.size(), id);
                activiteRepository.deleteByProjetId(id);
            }
            
            // 7. Supprimer les assignements du projet (travailler_projet)
            List<TravaillerProjet> travaillerProjets = travaillerProjetRepository.findByProjetId(id);
            if (!travaillerProjets.isEmpty()) {
                log.info("Suppression de {} relations employé-projet", travaillerProjets.size());
                travaillerProjetRepository.deleteAll(travaillerProjets);
            }

            // 8. Supprimer les dépôts du projet
            List<Depot> depots = depotRepository.findByProjetId(id);
            if (!depots.isEmpty()) {
                log.info("Suppression de {} dépôts pour le projet {}", depots.size(), id);
                depotRepository.deleteAll(depots);
            }

            // 9. Supprimer le projet lui-même
            projetRepository.delete(projet);
            log.info("Projet {} supprimé avec succès (toutes les relations nettoyées)", id);
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du projet {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression du projet: " + e.getMessage());
        }
    }

    private void validateDates(LocalDate dateDebut, LocalDate dateLimite) {
        // Vérifier que la date de début n'est pas antérieure à aujourd'hui
        if (dateDebut.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La date du projet ne peut pas être antérieure à la date d'aujourd'hui (" 
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + "). Veuillez saisir une date valide."
            );
        }
        
        // Vérifier que la date de début n'est pas postérieure à la date limite
        if (dateDebut.isAfter(dateLimite)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La date de début ne peut être postérieure à la date limite"
            );
        }
    }

    private ProjetResponse mapToResponse(Projet projet) {
        ProjetResponse response = new ProjetResponse();
        response.setId(projet.getId());
        response.setNom(projet.getNom());
        response.setDescription(projet.getDescription());
        response.setBudget(projet.getBudget());
        response.setDateDebut(projet.getDateDebut());
        response.setDateLimite(projet.getDateLimite());
        response.setProgression(projet.getProgression());
        response.setStatut(projet.getStatut() != null ? projet.getStatut().name() : determinerStatut(projet));
        response.setEstDeposé(projet.isEstDeposé());
        response.setJoursRestants(calculerJoursRestants(projet));
        
        // Mapper le client
        if (projet.getClient() != null) {
            ProjetResponse.ClientInfo clientInfo = new ProjetResponse.ClientInfo();
            clientInfo.setId(projet.getClient().getId());
            clientInfo.setNom(projet.getClient().getNom());
            clientInfo.setPrenom(projet.getClient().getPrenom());
            response.setClient(clientInfo);
        }
        
        // Mapper le chef de projet
        if (projet.getChefProjet() != null) {
            ProjetResponse.EmployeInfo chefInfo = new ProjetResponse.EmployeInfo();
            chefInfo.setId(projet.getChefProjet().getId());
            chefInfo.setNom(projet.getChefProjet().getNom());
            chefInfo.setPrenom(projet.getChefProjet().getPrenom());
            chefInfo.setPoste("Chef de Projet");
            response.setChefDeProjet(chefInfo);
        }
        
        // Mapper les employés via TravaillerProjet
        if (projet.getTravaillerProjets() != null && !projet.getTravaillerProjets().isEmpty()) {
            List<ProjetResponse.EmployeInfo> employesInfo = projet.getTravaillerProjets().stream()
                    .map(tp -> {
                        ProjetResponse.EmployeInfo employeInfo = new ProjetResponse.EmployeInfo();
                        employeInfo.setId(tp.getEmploye().getId());
                        employeInfo.setNom(tp.getEmploye().getNom());
                        employeInfo.setPrenom(tp.getEmploye().getPrenom());
                        employeInfo.setPoste(tp.getEmploye().getPoste());
                        return employeInfo;
                    })
                    .collect(Collectors.toList());
            response.setEmployes(employesInfo);
        }

        // Mapper les dépôts
        List<Depot> depots = depotRepository.findByProjetId(projet.getId());
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
                        // IDs de relation (dépôt de projet uniquement)
                        depotResponse.setTacheId(null);
                        depotResponse.setActiviteId(null);
                        depotResponse.setProjetId(projet.getId());
                        return depotResponse;
                    })
                    .collect(Collectors.toList());
            response.setDepots(depotsResponse);
        }

        return response;
    }

    private String determinerStatut(Projet projet) {
        LocalDate aujourdHui = LocalDate.now();
        
        if (projet.getProgression() >= 100) {
            return "Terminé";
        } else if (aujourdHui.isAfter(projet.getDateLimite())) {
            return "En retard";
        } else if (aujourdHui.isBefore(projet.getDateDebut())) {
            return "Non démarré";
        } else {
            return "En cours";
        }
    }

    @Transactional
    public ProjetResponse updateProjetStatut(Long id, StatutProjet statut) {
        log.info("Mise à jour du statut du projet {} à {}", id, statut);
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));
        projet.setStatut(statut);
        projet = projetRepository.save(projet);
        return mapToResponse(projet);
    }

    @Transactional
    public ProjetResponse deposerProjet(Long id, DepotRequest depotRequest, MultipartFile file) throws IOException {
        log.info("Dépôt du projet {}", id);
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));
        projet.setEstDeposé(true);
        projet.setStatut(StatutProjet.TERMINE);
        projet = projetRepository.save(projet);

        // Chercher un dépôt existant pour ce projet
        List<Depot> existingDepots = depotRepository.findByProjetId(id);
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
        depot.setProjet(projet);
        depotRepository.save(depot);

        // Forcer la progression à 100% car le projet est déposé
        projet.setProgression(100);
        projetRepository.save(projet);

        log.info("Projet {} déposé et marqué comme terminé avec dépôt {}", id, depot.getId());
        return mapToResponse(projet);
    }

    public boolean hasDepot(Long projetId) {
        List<Depot> depots = depotRepository.findByProjetId(projetId);
        return !depots.isEmpty();
    }

    public boolean areAllActivitesDeposees(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        if (projet.getActivites() == null || projet.getActivites().isEmpty()) {
            return false;
        }
        return projet.getActivites().stream().allMatch(Activite::isEstDeposé);
    }

    public String getDepotFilePath(Long depotId) {
        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new RuntimeException("Dépôt non trouvé avec l'id: " + depotId));
        return depot.getCheminFichier();
    }

    private Integer calculerJoursRestants(Projet projet) {
        LocalDate aujourdHui = LocalDate.now();
        if (aujourdHui.isAfter(projet.getDateLimite())) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(projet.getDateLimite(), aujourdHui) * -1;
        } else {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(aujourdHui, projet.getDateLimite());
        }
    }
}
