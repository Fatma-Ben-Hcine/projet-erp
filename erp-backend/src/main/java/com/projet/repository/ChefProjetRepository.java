package com.projet.repository;

import com.projet.entity.ChefProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChefProjetRepository extends JpaRepository<ChefProjet, Long> {
}
