package com.projet.config;

import com.projet.entity.Admin;
import com.projet.enums.Role;
import com.projet.enums.TypeUtilisateur;
import com.projet.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - Initialise les données de l'application au démarrage
 * Crée un utilisateur admin par défaut s'il n'existe pas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeAdminUser();
    }

    /**
     * Crée un utilisateur admin par défaut s'il n'existe pas
     */
    private void initializeAdminUser() {
        String adminEmail = "admin@erp.com";

        // Vérifier si l'admin existe déjà
        if (utilisateurRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin utilisateur existe déjà: {}", adminEmail);
            return;
        }

        // Créer un nouvel utilisateur admin
        Admin admin = Admin.builder()
                .nom("Admin")
                .prenom("Système")
                .email(adminEmail)
                .motDePasse(passwordEncoder.encode("Admin@123")) // Mot de passe par défaut (à changer)
                .CIN("00000000000")
                .numeroTel("+212 6 00 00 00 00")
                .poste("Administrateur Système")
                .competences("Gestion du système, Configuration, Maintenance")
                .role(Role.ROLE_ADMIN)
                .typeUtilisateur(TypeUtilisateur.PERMANENT)
                .actif(true)
                .photo(null)
                .build();

        utilisateurRepository.save(admin);
        log.info("✓ Utilisateur admin créé avec succès");
        log.info("  Email: {}", adminEmail);
        log.info("  Mot de passe par défaut: Admin@123 (à changer lors de la première connexion)");
    }
}
