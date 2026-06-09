package com.projet.repository;

import com.projet.entity.HeureSupplementaire;
import com.projet.entity.StatutHeureSupplementaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeureSupplementaireRepository extends JpaRepository<HeureSupplementaire, Long> {
    
    List<HeureSupplementaire> findByEmployeId(Long employeId);
    
    List<HeureSupplementaire> findByStatut(StatutHeureSupplementaire statut);
    
    @Query("SELECT h FROM HeureSupplementaire h LEFT JOIN FETCH h.employe")
    List<HeureSupplementaire> findAllWithEmploye();
}
