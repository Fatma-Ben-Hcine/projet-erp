package com.projet.repository;

import com.projet.entity.TravaillerProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravaillerProjetRepository extends JpaRepository<TravaillerProjet, Long> {
    List<TravaillerProjet> findByProjetId(Long projetId);
    List<TravaillerProjet> findByEmployeId(Long employeId);
    Optional<TravaillerProjet> findByEmployeIdAndProjetId(Long employeId, Long projetId);
    void deleteByProjetId(Long projetId);
}
