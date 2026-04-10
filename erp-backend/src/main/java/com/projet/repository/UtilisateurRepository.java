package com.projet.repository;

import com.projet.entity.Utilisateur;
import com.projet.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByEmail(String email);
    
    Optional<Utilisateur> findByCIN(String CIN);
    
    List<Utilisateur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrPosteContainingIgnoreCase(
        String nom, String prenom, String poste
    );

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByRoleAndNomContainingIgnoreCaseOrRoleAndPrenomContainingIgnoreCaseOrRoleAndPosteContainingIgnoreCase(
        Role role, String nom,
        Role role2, String prenom,
        Role role3, String poste
    );
}
