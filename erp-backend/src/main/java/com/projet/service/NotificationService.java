package com.projet.service;

import com.projet.entity.Employe;
import com.projet.entity.Notification;
import com.projet.entity.Projet;
import com.projet.entity.TravaillerProjet;
import com.projet.enums.Role;
import com.projet.enums.StatutProjet;
import com.projet.repository.EmployeRepository;
import com.projet.repository.NotificationRepository;
import com.projet.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projet.dto.NotificationDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProjetRepository projetRepository;
    private final EmployeRepository employeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Créer une notification pour un employé
     */
    @Transactional
    public Notification creerNotification(
            Employe destinataire,
            String message,
            String type,
            Long projetId,
            String projetNom) {

        Notification notif = new Notification();
        notif.setDestinataire(destinataire);
        notif.setMessage(message);
        notif.setType(type);
        notif.setProjetId(projetId);
        notif.setProjetNom(projetNom);
        notif.setEstLue(false);
        notif.setDateCreation(LocalDateTime.now());

        Notification saved = notificationRepository.save(notif);

        // Envoyer via WebSocket au destinataire
        envoyerNotificationWebSocket(destinataire.getId(), saved);

        return saved;
    }

    /**
     * Envoyer une notification via WebSocket
     */
    private void envoyerNotificationWebSocket(Long employeId, Notification notification) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + employeId,
                    notification
            );
            log.info("Notification WebSocket envoyée à l'employé {}", employeId);
        } catch (Exception e) {
            log.error("Erreur envoi WebSocket notification: {}", e.getMessage());
        }
    }

    /**
     * Envoyer le compteur de notifications non lues via WebSocket
     */
    public void envoyerCompteurNonLues(Long employeId) {
        try {
            long count = countByDestinataireIdAndEstLueFalse(employeId);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/count/" + employeId,
                    count
            );
        } catch (Exception e) {
            log.error("Erreur envoi compteur WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Notifier quand un employé est assigné à un projet
     */
    @Transactional
    public void notifierProjetAssigne(Employe employe, Projet projet) {
        Notification notif = creerNotification(
                employe,
                "Vous avez été assigné au projet \"" + projet.getNom() + "\"",
                "PROJET_ASSIGNE",
                projet.getId(),
                projet.getNom()
        );

        log.info("Notification PROJET_ASSIGNE créée pour employé {} sur projet {}",
                employe.getId(), projet.getId());
    }

    /**
     * Cron job : vérifier les projets dont la date limite est dans 3 jours
     * Exécuté tous les jours à 8h du matin
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void notifierDateLimiteProche() {
        log.info("Exécution du cron job: vérification des dates limites");

        LocalDate dans3Jours = LocalDate.now().plusDays(3);
        List<Projet> projets = projetRepository.findByDateLimiteAndStatutNot(
                dans3Jours, StatutProjet.TERMINE);

        log.info("{} projets trouvés avec date limite dans 3 jours", projets.size());

        for (Projet projet : projets) {
            String message = "⚠️ Le projet \"" + projet.getNom()
                    + "\" arrive à échéance le "
                    + projet.getDateLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Récupérer les employés assignés au projet via TravaillerProjet
            Set<Long> employeIdsNotifies = new HashSet<>();

            // Notifier tous les membres
            for (TravaillerProjet tp : projet.getTravaillerProjets()) {
                Employe membre = tp.getEmploye();
                if (membre != null && employeIdsNotifies.add(membre.getId())) {
                    creerNotification(membre, message,
                            "DATE_LIMITE_PROCHE",
                            projet.getId(), projet.getNom());
                }
            }

            // Notifier le chef de projet s'il n'est pas déjà notifié
            if (projet.getChefProjet() != null) {
                if (employeIdsNotifies.add(projet.getChefProjet().getId())) {
                    creerNotification(projet.getChefProjet(), message,
                            "DATE_LIMITE_PROCHE",
                            projet.getId(), projet.getNom());
                }
            }

            // Notifier tous les admins
            List<Employe> admins = employeRepository.findByRoleAndActif(Role.ROLE_ADMIN);
            for (Employe admin : admins) {
                if (employeIdsNotifies.add(admin.getId())) {
                    creerNotification(admin, message,
                            "DATE_LIMITE_PROCHE",
                            projet.getId(), projet.getNom());
                }
            }
        }
    }

    /**
     * Marquer une notification comme lue
     */
    @Transactional
    public void marquerCommeLue(Long notifId) {
        Notification notif = notificationRepository.findById(notifId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        notif.setEstLue(true);
        notificationRepository.save(notif);

        // Mettre à jour le compteur via WebSocket
        envoyerCompteurNonLues(notif.getDestinataire().getId());
    }

    /**
     * Marquer toutes les notifications comme lues pour un employé
     */
    @Transactional
    public void marquerToutesCommeLues(Long employeId) {
        List<Notification> nonLues = notificationRepository.findByDestinataireIdAndEstLueFalse(employeId);
        nonLues.forEach(n -> n.setEstLue(true));
        notificationRepository.saveAll(nonLues);

        // Mettre à jour le compteur via WebSocket
        envoyerCompteurNonLues(employeId);
    }

    /**
     * Mapper une Notification en NotificationDTO
     */
    public NotificationDTO mapToDTO(Notification notif) {
        return NotificationDTO.builder()
                .id(notif.getId())
                .message(notif.getMessage())
                .type(notif.getType())
                .projetId(notif.getProjetId())
                .projetNom(notif.getProjetNom())
                .estLue(notif.isEstLue())
                .dateCreation(notif.getDateCreation() != null
                        ? notif.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : null)
                .build();
    }

    /**
     * Récupérer les notifications d'un employé en DTO
     */
    public List<NotificationDTO> getNotificationsDTOByEmployeId(Long employeId) {
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(employeId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les notifications d'un employé (entité)
     */
    public List<Notification> getNotificationsByEmployeId(Long employeId) {
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(employeId);
    }

    /**
     * Compter les notifications non lues
     */
    public long countByDestinataireIdAndEstLueFalse(Long employeId) {
        return notificationRepository.countByDestinataireIdAndEstLueFalse(employeId);
    }
}
