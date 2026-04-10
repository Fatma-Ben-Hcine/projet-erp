package com.projet.repository;

import com.projet.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByActiviteId(Long activiteId);
    
    Optional<Tache> findByIdAndActiviteId(Long id, Long activiteId);
    
    @Query("SELECT t FROM Tache t WHERE t.activite.id = :activiteId ORDER BY t.dateDebut ASC")
    List<Tache> findByActiviteIdOrderByDateDebut(@Param("activiteId") Long activiteId);
    
    @Query("SELECT COUNT(t) FROM Tache t WHERE t.activite.id = :activiteId")
    long countByActiviteId(@Param("activiteId") Long activiteId);
    
    @Query("SELECT t FROM Tache t JOIN t.travaillerTaches tt WHERE tt.employe.id = :employeId")
    List<Tache> findByEmployeId(@Param("employeId") Long employeId);
    
    boolean existsByIdAndActiviteId(Long id, Long activiteId);
}
