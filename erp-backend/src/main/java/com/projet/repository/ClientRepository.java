package com.projet.repository;

import com.projet.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMatriculeFiscale(String matriculeFiscale);
    List<Client> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
        String nom, String prenom);
}
