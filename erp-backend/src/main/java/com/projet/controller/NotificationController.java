package com.projet.controller;

import com.projet.entity.Employe;
import com.projet.entity.Utilisateur;
import com.projet.repository.UtilisateurRepository;
import com.projet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    private Employe getCurrentEmploye(Authentication auth) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (utilisateur instanceof Employe) {
            return (Employe) utilisateur;
        }
        return null;
    }

    /**
     * Récupérer mes notifications
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMesNotifications(Authentication auth) {
        Employe employe = getCurrentEmploye(auth);
        if (employe == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(notificationService.getNotificationsDTOByEmployeId(employe.getId()));
    }

    /**
     * Compter les notifications non lues
     */
    @GetMapping("/non-lues/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> compterNonLues(Authentication auth) {
        Employe employe = getCurrentEmploye(auth);
        Map<String, Long> response = new HashMap<>();
        if (employe == null) {
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }
        long count = notificationService.countByDestinataireIdAndEstLueFalse(employe.getId());
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer une notification comme lue
     */
    @PatchMapping("/{id}/lue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> marquerLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Marquée comme lue");
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PatchMapping("/tout-lire")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> marquerToutesLues(Authentication auth) {
        Employe employe = getCurrentEmploye(auth);
        if (employe != null) {
            notificationService.marquerToutesCommeLues(employe.getId());
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Toutes lues");
        return ResponseEntity.ok(response);
    }
}
