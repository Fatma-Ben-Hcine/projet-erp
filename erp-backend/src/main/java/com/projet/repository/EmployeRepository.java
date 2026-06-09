package com.projet.repository;

import com.projet.entity.Employe;
import com.projet.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    List<Employe> findByActifTrue();
    
    Optional<Employe> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT e FROM Employe e WHERE e.role = :role AND e.actif = true")
    List<Employe> findByRoleAndActif(@Param("role") Role role);
    
    @Query("SELECT e FROM Employe e WHERE e.actif = true ORDER BY e.nom, e.prenom")
    List<Employe> findAllActifsOrderByNomPrenom();
    
    @Query("SELECT COUNT(e) FROM Employe e WHERE e.actif = true")
    long countActifs();
    
    @Query("SELECT e FROM Employe e JOIN e.travaillerActivites ta WHERE ta.activite.id = :activiteId")
    List<Employe> findByActiviteId(@Param("activiteId") Long activiteId);
    
    @Query("SELECT e FROM Employe e JOIN e.travaillerTaches tt WHERE tt.tache.id = :tacheId")
    List<Employe> findByTacheId(@Param("tacheId") Long tacheId);
    
    @Query("SELECT e FROM Employe e WHERE e.role IN :roles AND e.actif = true")
    List<Employe> findByRolesInAndActif(@Param("roles") List<Role> roles);
}
