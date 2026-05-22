package com.projet.controller;

import com.projet.dto.DashboardStatsDTO;
import com.projet.entity.Employe;
import com.projet.entity.Utilisateur;
import com.projet.enums.Role;
import com.projet.enums.StatutProjet;
import com.projet.enums.TypeUtilisateur;
import com.projet.repository.ActiviteRepository;
import com.projet.repository.EmployeRepository;
import com.projet.repository.ProjetRepository;
import com.projet.repository.TacheRepository;
import com.projet.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/stats")
@CrossOrigin(origins = "*")
public class DashboardStatsController {

    @Autowired
    private ProjetRepository projetRepo;
    
    @Autowired
    private ActiviteRepository activiteRepo;
    
    @Autowired
    private TacheRepository tacheRepo;
    
    @Autowired
    private UtilisateurRepository utilisateurRepo;
    
    @Autowired
    private EmployeRepository employeRepo;

    @GetMapping
    public ResponseEntity<DashboardStatsDTO> getStats() {
        
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // ── Projets ──
        stats.setTotalProjets(projetRepo.count());
        stats.setRetard(projetRepo.countByStatut(StatutProjet.EN_RETARD));
        stats.setTermine(projetRepo.countByStatut(StatutProjet.TERMINE));

        // ── Activités & Tâches ──
        stats.setTotalActivites(activiteRepo.count());
        stats.setTotalTaches(tacheRepo.count());

        // ── Utilisateurs ──
        stats.setTotalUtilisateursPermanents(utilisateurRepo.countByTypeUtilisateur(TypeUtilisateur.PERMANENT));
        stats.setTotalEmployes(employeRepo.count());
        stats.setTotalAdministrateurs(utilisateurRepo.countByRole(Role.ROLE_ADMIN));
        stats.setTotalUtilisateursTemporaires(utilisateurRepo.countByTypeUtilisateur(TypeUtilisateur.TEMPORAIRE));

        return ResponseEntity.ok(stats);
    }
}
