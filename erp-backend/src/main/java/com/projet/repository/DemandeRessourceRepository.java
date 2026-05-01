package com.projet.repository;

import com.projet.entity.DemandeRessource;
import com.projet.entity.Employe;
import com.projet.entity.Ressource;
import com.projet.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRessourceRepository extends JpaRepository<DemandeRessource, Long> {

    // Demandes d'un employé spécifique
    List<DemandeRessource> findByEmployeId(Long employeId);

    // Demandes pour une ressource spécifique
    List<DemandeRessource> findByRessourceId(Long ressourceId);

    // Vérifier si une demande existe déjà pour cette ressource par cet employé
    Optional<DemandeRessource> findByRessourceAndEmploye(Ressource ressource, Employe employe);

    // Demandes par statut
    List<DemandeRessource> findByStatutDemande(StatutDemande statutDemande);

    // Compter les demandes pour une ressource avec un statut spécifique
    long countByRessourceIdAndStatutDemande(Long ressourceId, StatutDemande statut);
}
