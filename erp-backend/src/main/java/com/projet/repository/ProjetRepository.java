package com.projet.repository;

import com.projet.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {
    List<Projet> findByNomContainingIgnoreCase(String nom);
    List<Projet> findByProgressionLessThan(Integer progression);
    List<Projet> findByDateLimiteBefore(java.time.LocalDate date);
}
