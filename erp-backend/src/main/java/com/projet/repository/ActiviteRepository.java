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
    
    @Query("SELECT COUNT(a) FROM Activite a WHERE a.projet.id = :projetId")
    long countByProjetId(@Param("projetId") Long projetId);
    
    @Query("SELECT a FROM Activite a JOIN a.travaillerActivites ta WHERE ta.employe.id = :employeId")
    List<Activite> findByEmployeId(@Param("employeId") Long employeId);
    
    boolean existsByIdAndProjetId(Long id, Long projetId);
    
    void deleteByProjetId(Long projetId);
}
