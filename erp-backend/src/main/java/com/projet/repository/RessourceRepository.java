package com.projet.repository;

import com.projet.entity.Ressource;
import com.projet.enums.StatutRessource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    // Méthodes pour la logique simplifiée
    
    // Ressources par statut
    List<Ressource> findByStatut(StatutRessource statut);
    
    // Ressource par nom
    List<Ressource> findByNom(String nom);
}
