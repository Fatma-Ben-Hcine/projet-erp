package com.projet.service;

import com.projet.dto.ForgotPasswordRequest;
import com.projet.dto.ResetPasswordRequest;
import com.projet.entity.PasswordResetToken;
import com.projet.entity.Utilisateur;
import com.projet.repository.PasswordResetTokenRepository;
import com.projet.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendResetEmail(ForgotPasswordRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur trouvé avec cet email"));

        // Supprimer les anciens tokens pour cet utilisateur
        passwordResetTokenRepository.deleteByUtilisateur(utilisateur);

        // Générer un nouveau token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken passwordResetToken = new PasswordResetToken(resetToken, utilisateur, expiryDate);
        passwordResetTokenRepository.save(passwordResetToken);

        // Envoyer l'email
        String resetLink = "http://localhost:4300/reset-password?token=" + resetToken;
        sendEmail(utilisateur.getEmail(), utilisateur.getPrenom(), resetLink);

        log.info("Email de réinitialisation envoyé à: {}", utilisateur.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token de réinitialisation invalide"));

        if (token.isExpired()) {
            throw new RuntimeException("Le token de réinitialisation a expiré");
        }

        if (token.isUsed()) {
            throw new RuntimeException("Le token de réinitialisation a déjà été utilisé");
        }

        // Mettre à jour le mot de passe de l'utilisateur
        Utilisateur utilisateur = token.getUtilisateur();
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(utilisateur);

        // Marquer le token comme utilisé
        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        log.info("Mot de passe réinitialisé pour l'utilisateur: {}", utilisateur.getEmail());
    }

    private void sendEmail(String toEmail, String prenom, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText(generateEmailContent(prenom, resetLink));
        mailSender.send(message);
    }

    private String generateEmailContent(String prenom, String resetLink) {
        return "Bonjour " + prenom + ",\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n" +
                resetLink + "\n\n" +
                "Ce lien expirera dans 15 minutes.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe de support";
    }
}
