package com.projet.repository;

import com.projet.entity.Ressource;
import com.projet.enums.SituationRessource;
import com.projet.enums.StatutRessource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    // Nouvelles méthodes pour la logique simplifiée
    
    // Ressources actives (pour les employés)
    List<Ressource> findByStatut(StatutRessource statut);
    
    // Ressources demandées par un employé spécifique
    List<Ressource> findByEmployeDemandeurIdAndSituation(Long employeId, SituationRessource situation);

    // Anciennes méthodes (conservées pour compatibilité)
    List<Ressource> findByStatutAndSituation(StatutRessource statut, SituationRessource situation);

    // Ressources avec abonnement expirant bientôt
    @Query("SELECT r FROM Ressource r WHERE r.dateFinAbonnement BETWEEN :today AND :limitDate")
    List<Ressource> findRessourcesWithAbonnementExpiringSoon(@Param("today") LocalDate today, @Param("limitDate") LocalDate limitDate);

    // Ressources avec abonnement expiré
    @Query("SELECT r FROM Ressource r WHERE r.dateFinAbonnement < :today")
    List<Ressource> findRessourcesWithAbonnementExpired(@Param("today") LocalDate today);

    // Ressources avec dates d'abonnement renseignées
    @Query("SELECT r FROM Ressource r WHERE r.dateDebutAbonnement IS NOT NULL AND r.dateFinAbonnement IS NOT NULL")
    List<Ressource> findAllWithAbonnementDates();

    // Ressources sans dates d'abonnement
    @Query("SELECT r FROM Ressource r WHERE r.dateDebutAbonnement IS NULL OR r.dateFinAbonnement IS NULL")
    List<Ressource> findAllWithoutAbonnementDates();
}
