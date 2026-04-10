package com.projet.service;

import com.projet.dto.ProjetRequest;
import com.projet.dto.ProjetResponse;
import com.projet.entity.Projet;
import com.projet.entity.TravaillerProjet;
import com.projet.entity.Employe;
import com.projet.entity.Client;
import com.projet.entity.Utilisateur;
import com.projet.enums.Role;
import com.projet.repository.ProjetRepository;
import com.projet.repository.TravaillerProjetRepository;
import com.projet.repository.ClientRepository;
import com.projet.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final TravaillerProjetRepository travaillerProjetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;

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
                            tp.setStatut("ASSIGNÉ");
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

        Projet updated = projetRepository.save(projet);
        log.info("Projet mis à jour: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProjet(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + id));
        projetRepository.delete(projet);
        log.info("Projet supprimé: {}", id);
    }

    private void validateDates(LocalDate dateDebut, LocalDate dateLimite) {
        if (dateDebut.isAfter(dateLimite)) {
            throw new RuntimeException("La date de début ne peut être postérieure à la date limite");
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
        response.setStatut(determinerStatut(projet));
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

    private Integer calculerJoursRestants(Projet projet) {
        LocalDate aujourdHui = LocalDate.now();
        if (aujourdHui.isAfter(projet.getDateLimite())) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(projet.getDateLimite(), aujourdHui) * -1;
        } else {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(aujourdHui, projet.getDateLimite());
        }
    }
}
