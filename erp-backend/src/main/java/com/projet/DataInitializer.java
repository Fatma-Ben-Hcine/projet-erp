package com.projet;

import com.projet.entity.Admin;
import com.projet.entity.Employe;
import com.projet.enums.TypeUtilisateur;
import com.projet.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeData();
    }

    private void initializeData() {
        if (utilisateurRepository.count() == 0) {
            log.info("Initialisation des données de test...");

            Admin admin2 = new Admin();
            admin2.setNom("Besbes");
            admin2.setPrenom("Klod");
            admin2.setEmail("klodbesbes@gmail.com");
            admin2.setMotDePasse(passwordEncoder.encode("admin123"));
            admin2.setCIN("12345678");
            admin2.setNumeroTel("0600000000");
            admin2.setPoste("Administrateur Système");
            admin2.setCompetences("Gestion système, administration, sécurité");
            admin2.setTypeUtilisateur(TypeUtilisateur.PERMANENT);
            admin2.setActif(true);

            Admin admin = new Admin();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setEmail("admin@projet.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setCIN("ADMIN123456");
            admin.setNumeroTel("0600000000");
            admin.setPoste("Administrateur Système");
            admin.setCompetences("Gestion système, administration, sécurité");
            admin.setTypeUtilisateur(TypeUtilisateur.PERMANENT);
            admin.setActif(true);

            Employe employe = new Employe();
            employe.setNom("Employe");
            employe.setPrenom("Test");
            employe.setEmail("employe@projet.com");
            employe.setMotDePasse(passwordEncoder.encode("employe123"));
            employe.setCIN("EMP123456");
            employe.setNumeroTel("0611111111");
            employe.setPoste("Employé");
            employe.setCompetences("Développement, maintenance, support");
            employe.setTypeUtilisateur(TypeUtilisateur.PERMANENT);
            employe.setActif(true);

            utilisateurRepository.save(admin);
            utilisateurRepository.save(admin2);
            utilisateurRepository.save(employe);

            log.info("Données initialisées avec succès !");
            log.info("Admin: admin@projet.com / admin123");
            log.info("Employé: employe@projet.com / employe123");
        } else {
            log.info("Les données existent déjà, pas d'initialisation nécessaire.");
        }
    }
}
