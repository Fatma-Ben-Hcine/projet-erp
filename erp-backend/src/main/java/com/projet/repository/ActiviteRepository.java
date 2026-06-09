package com.projet.repository;

import com.projet.entity.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    List<Activite> findByProjetId(Long projetId);
    
    Optional<Activite> findByIdAndProjetId(Long id, Long projetId);
    
    @Query("SELECT a FROM Activite a WHERE a.projet.id = :projetId ORDER BY a.dateDebut ASC")
    List<Activite> findByProjetIdOrderByDateDebut(@Param("projetId") Long projetId);
    
    @Query("SELECT DISTINCT a FROM Activite a LEFT JOIN FETCH a.travaillerActivites ta LEFT JOIN FETCH ta.employe LEFT JOIN FETCH a.taches t LEFT JOIN FETCH t.travaillerTaches tt LEFT JOIN FETCH tt.employe WHERE a.projet.id = :projetId ORDER BY a.dateDebut ASC")
    List<Activite> findByProjetIdWithEmployesAndTaches(@Param("projetId") Long projetId);
    
    @Query("SELECT COUNT(a) FROM Activite a WHERE a.projet.id = :projetId")
    long countByProjetId(@Param("projetId") Long projetId);
    
    @Query("SELECT a FROM Activite a JOIN a.travaillerActivites ta WHERE ta.employe.id = :employeId")
    List<Activite> findByEmployeId(@Param("employeId") Long employeId);
    
    @Query("SELECT ta.employe FROM TravaillerActivite ta WHERE ta.id.activiteId = :activiteId")
    List<com.projet.entity.Employe> findEmployesByActiviteId(@Param("activiteId") Long activiteId);

    @Query("SELECT a FROM Activite a JOIN FETCH a.projet WHERE a.id = :id")
    Optional<Activite> findByIdWithProjet(@Param("id") Long id);

    boolean existsByIdAndProjetId(Long id, Long projetId);
    
    void deleteByProjetId(Long projetId);
}
