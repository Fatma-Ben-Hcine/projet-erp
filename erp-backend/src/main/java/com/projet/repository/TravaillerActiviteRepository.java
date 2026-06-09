package com.projet.repository;

import com.projet.entity.TravaillerActivite;
import com.projet.entity.TravaillerActiviteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravaillerActiviteRepository extends JpaRepository<TravaillerActivite, TravaillerActiviteId> {

    List<TravaillerActivite> findByEmployeId(Long employeId);
    
    List<TravaillerActivite> findByActiviteId(Long activiteId);
    
    @Query("SELECT ta FROM TravaillerActivite ta LEFT JOIN FETCH ta.employe WHERE ta.id.activiteId = :activiteId")
    List<TravaillerActivite> findByActiviteIdWithQuery(@Param("activiteId") Long activiteId);
    
    Optional<TravaillerActivite> findByEmployeIdAndActiviteId(Long employeId, Long activiteId);
    
    @Query("SELECT ta FROM TravaillerActivite ta WHERE ta.employe.id = :employeId AND ta.activite.id = :activiteId")
    Optional<TravaillerActivite> findByEmployeAndActivite(@Param("employeId") Long employeId, @Param("activiteId") Long activiteId);
    
    @Query("SELECT COUNT(ta) FROM TravaillerActivite ta WHERE ta.activite.id = :activiteId")
    long countByActiviteId(@Param("activiteId") Long activiteId);
    
    // Méthode supprimée : la progression est calculée dynamiquement
    // @Query("SELECT AVG(ta.progression) FROM TravaillerActivite ta WHERE ta.activite.id = :activiteId")
    // Double getAverageProgressionByActiviteId(@Param("activiteId") Long activiteId);
    
    void deleteByEmployeIdAndActiviteId(Long employeId, Long activiteId);
    
    void deleteByActiviteId(Long activiteId);
}
