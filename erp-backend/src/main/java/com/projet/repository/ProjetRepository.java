package com.projet.repository;

import com.projet.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    List<Projet> findByNomContainingIgnoreCase(String nom);
    List<Projet> findByProgressionLessThan(Integer progression);
    List<Projet> findByDateLimiteBefore(java.time.LocalDate date);
    
    @Query("SELECT p FROM Projet p LEFT JOIN FETCH p.travaillerProjets WHERE p.id = :id")
    Optional<Projet> findByIdWithEmployes(@Param("id") Long id);
    
    // Check if a user is chef de projet for a specific project
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Projet p WHERE p.id = :projetId AND p.chefProjet.id = :employeId")
    boolean isChefDeProjet(@Param("projetId") Long projetId, @Param("employeId") Long employeId);
}
