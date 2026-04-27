package com.projet.service;

import com.projet.entity.Activite;
import com.projet.entity.Projet;
import com.projet.repository.ActiviteRepository;
import com.projet.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjetProgressionService {

    private final ProjetRepository projetRepository;
    private final ActiviteRepository activiteRepository;

    /**
     * Recalcule et sauvegarde la progression du projet
     * basée sur le nombre d'activités déposées.
     * 
     * Règle : (nb activités avec est_depose=true / nb total activités) × 100
     * Si le projet est déposé → progression forcée à 100%
     */
    @Transactional
    public int recalculerEtSauvegarder(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'id: " + projetId));
        
        List<Activite> activites = activiteRepository.findByProjetId(projetId);
        
        int progression = 0;
        
        if (activites != null && !activites.isEmpty()) {
            long nbDeposees = activites.stream()
                    .filter(Activite::isEstDeposé)
                    .count();
            progression = (int) Math.round(
                    (double) nbDeposees / activites.size() * 100
            );
            log.info("Projet {}: {}/{} activités déposées → progression {}%", 
                    projetId, nbDeposees, activites.size(), progression);
        } else {
            log.info("Projet {}: aucune activité → progression 0%", projetId);
        }
        
        // Si le projet lui-même est déposé → forcé à 100%
        if (projet.isEstDeposé()) {
            progression = 100;
            log.info("Projet {} est déposé → progression forcée à 100%", projetId);
        }
        
        projet.setProgression(progression);
        projetRepository.save(projet);
        
        return progression;
    }
}
