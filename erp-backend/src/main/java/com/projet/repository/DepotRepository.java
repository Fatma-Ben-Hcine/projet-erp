package com.projet.repository;

import com.projet.entity.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    
    // Requêtes corrigées pour distinguer les niveaux hiérarchiques
    
    /**
     * Dépôts de PROJET uniquement (pas d'activité, pas de tâche)
     */
    @Query("SELECT d FROM Depot d WHERE d.projet.id = :projetId AND d.activite IS NULL AND d.tache IS NULL")
    List<Depot> findDepotsByProjetIdSeulement(@Param("projetId") Long projetId);
    
    /**
     * Dépôts d'ACTIVITÉ uniquement (activite_id rempli, tache_id NULL)
     */
    @Query("SELECT d FROM Depot d WHERE d.activite.id = :activiteId AND d.tache IS NULL")
    List<Depot> findDepotsByActiviteIdSeulement(@Param("activiteId") Long activiteId);
    
    /**
     * Dépôts de TÂCHE uniquement (tache_id rempli)
     * Note: un dépôt de tâche a aussi activite_id et projet_id remplis
     */
    @Query("SELECT d FROM Depot d WHERE d.tache.id = :tacheId")
    List<Depot> findDepotsByTacheId(@Param("tacheId") Long tacheId);
    
    // Anciennes méthodes (conservées pour compatibilité si besoin)
    List<Depot> findByProjetId(Long projetId);
    List<Depot> findByTacheId(Long tacheId);
    List<Depot> findByActiviteId(Long activiteId);
    
    void deleteByProjetId(Long projetId);
}
