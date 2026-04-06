package com.projet.repository;

import com.projet.entity.PasswordResetToken;
import com.projet.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUtilisateur(Utilisateur utilisateur);

    void deleteByUtilisateur(Utilisateur utilisateur);
}
