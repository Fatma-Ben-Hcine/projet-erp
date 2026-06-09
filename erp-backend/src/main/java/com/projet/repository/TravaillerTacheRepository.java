package com.projet.repository;

import com.projet.entity.TravaillerTache;
import com.projet.entity.TravaillerTacheId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravaillerTacheRepository extends JpaRepository<TravaillerTache, TravaillerTacheId> {

    List<TravaillerTache> findByEmployeId(Long employeId);
    
    List<TravaillerTache> findByTacheId(Long tacheId);
    
    Optional<TravaillerTache> findByEmployeIdAndTacheId(Long employeId, Long tacheId);
    
    @Query("SELECT tt FROM TravaillerTache tt WHERE tt.employe.id = :employeId AND tt.tache.id = :tacheId")
    Optional<TravaillerTache> findByEmployeAndTache(@Param("employeId") Long employeId, @Param("tacheId") Long tacheId);
    
    @Query("SELECT COUNT(tt) FROM TravaillerTache tt WHERE tt.tache.id = :tacheId")
    long countByTacheId(@Param("tacheId") Long tacheId);
    
        
    void deleteByEmployeIdAndTacheId(Long employeId, Long tacheId);
    
    void deleteByTacheId(Long tacheId);
}
