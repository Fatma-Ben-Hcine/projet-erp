package com.projet.repository;

import com.projet.entity.Conge;
import com.projet.entity.StatutConge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {
    
    List<Conge> findByEmployeId(Long employeId);
    
    List<Conge> findByStatut(StatutConge statut);
    
    @Query("SELECT c FROM Conge c LEFT JOIN FETCH c.employe")
    List<Conge> findAllWithEmploye();
}
