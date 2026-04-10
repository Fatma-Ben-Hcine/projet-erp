package com.projet.repository;

import com.projet.entity.Contrat;
import com.projet.enums.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByClientId(Long clientId);
    List<Contrat> findByStatut(StatutContrat statut);
}
