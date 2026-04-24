package com.projet.repository;

import com.projet.entity.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {
    List<Depot> findByProjetId(Long projetId);
    void deleteByProjetId(Long projetId);
}
