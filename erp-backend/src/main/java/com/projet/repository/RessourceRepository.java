package com.projet.repository;

import com.projet.entity.Ressource;
import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    // Méthodes pour la logique simplifiée
    
    // Ressources par statut
    List<Ressource> findByStatut(StatutRessource statut);
    
    // Ressources par situation
    List<Ressource> findBySituation(SituationRessource situation);
    
    // Ressources par statut et situation
    List<Ressource> findByStatutAndSituation(StatutRessource statut, SituationRessource situation);
    
    // Ressources demandées par un employé spécifique
    List<Ressource> findByEmployeDemandeurIdAndSituation(Long employeId, SituationRessource situation);
}
