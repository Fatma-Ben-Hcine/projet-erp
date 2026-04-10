package com.projet.service;

import com.projet.dto.CreateUtilisateurRequest;
import com.projet.dto.UpdateUtilisateurRequest;
import com.projet.dto.UtilisateurResponse;
import com.projet.entity.Admin;
import com.projet.entity.Employe;
import com.projet.entity.Utilisateur;
import com.projet.enums.Role;
import com.projet.enums.TypeUtilisateur;
import com.projet.exception.CINAlreadyExistsException;
import com.projet.exception.DuplicateUserException;
import com.projet.exception.EmailAlreadyExistsException;
import com.projet.exception.EntityNotFoundException;
import com.projet.exception.ValidationException;
import com.projet.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurManagementService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    @Transactional
    public UtilisateurResponse createUtilisateur(CreateUtilisateurRequest request) {
        log.info("Tentative de création d'utilisateur avec les données: {}", request);
        
        List<String> errors = new ArrayList<>();
        
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Email déjà existant: {}", request.getEmail());
            errors.add("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        // Vérifier si le CIN existe déjà
        if (utilisateurRepository.findByCIN(request.getCIN()).isPresent()) {
            log.error("CIN déjà existant: {}", request.getCIN());
            errors.add("Un utilisateur avec ce CIN existe déjà: " + request.getCIN());
        }
        
        // Si des erreurs existent, lancer une exception combinée
        if (!errors.isEmpty()) {
            String combinedMessage = String.join("; ", errors);
            throw new DuplicateUserException(combinedMessage);
        }

        // Créer l'utilisateur selon le rôle
        Utilisateur utilisateur;
        log.info("Rôle reçu: {}", request.getRole());
        
        if (request.getRole() == Role.ROLE_ADMIN) {
            utilisateur = new Admin();
            log.info("Création d'un Admin");
        } else {
            utilisateur = new Employe();
            log.info("Création d'un Employé");
        }

        // Mapper les champs
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setCIN(request.getCIN());
        utilisateur.setNumeroTel(request.getNumeroTel());
        utilisateur.setPoste(request.getPoste());
        utilisateur.setCompetences(request.getCompetences());
        utilisateur.setRole(request.getRole());
        
        // Utiliser le typeUtilisateur de la requête, ou PERMANENT par défaut
        TypeUtilisateur typeUtilisateur = request.getTypeUtilisateur() != null ? 
            request.getTypeUtilisateur() : TypeUtilisateur.PERMANENT;
        utilisateur.setTypeUtilisateur(typeUtilisateur);
        utilisateur.setActif(true);
        utilisateur.setPhoto(request.getPhoto());

        log.info("Utilisateur avant sauvegarde: {}", utilisateur);
        
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        log.info("Nouvel utilisateur créé avec ID: {}, Email: {}", 
                savedUtilisateur.getId(), savedUtilisateur.getEmail());
        
        return mapToResponse(savedUtilisateur);
    }

    @Transactional
    public UtilisateurResponse updateUtilisateur(Long id, UpdateUtilisateurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        // Collecter les erreurs de validation
        List<String> erreurs = new ArrayList<>();

        // Vérifier si le CIN est déjà utilisé par un autre utilisateur
        if (request.getCIN() != null && !request.getCIN().equals(utilisateur.getCIN())) {
            utilisateurRepository.findByCIN(request.getCIN())
                    .ifPresent(existingUser -> {
                        erreurs.add("Le CIN '" + request.getCIN() + "' est déjà utilisé par un autre utilisateur");
                    });
        }

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (request.getEmail() != null && !request.getEmail().equals(utilisateur.getEmail())) {
            utilisateurRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        erreurs.add("L'email '" + request.getEmail() + "' est déjà utilisé par un autre utilisateur");
                    });
        }

        // Si there are errors, throw a single exception with all messages
        if (!erreurs.isEmpty()) {
            throw new ValidationException(String.join("; ", erreurs));
        }

        // Mapper les champs (sauf motDePasse et role)
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setCIN(request.getCIN());
        utilisateur.setNumeroTel(request.getNumeroTel());
        utilisateur.setPoste(request.getPoste());
        utilisateur.setCompetences(request.getCompetences());
        
        // Mettre à jour le typeUtilisateur si fourni
        if (request.getTypeUtilisateur() != null) {
            utilisateur.setTypeUtilisateur(request.getTypeUtilisateur());
        }

        // Mettre à jour la photo si fournie
        if (request.getPhoto() != null) {
            utilisateur.setPhoto(request.getPhoto());
        }

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur mis à jour: {} avec typeUtilisateur: {}", 
                savedUtilisateur.getEmail(), savedUtilisateur.getTypeUtilisateur());
        
        return mapToResponse(savedUtilisateur);
    }

    @Transactional
    public String deleteUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        String email = utilisateur.getEmail();
        utilisateurRepository.delete(utilisateur);
        log.info("Utilisateur supprimé: {}", email);
        
        return "L'utilisateur " + email + " a été supprimé avec succès";
    }

    @Transactional
    public UtilisateurResponse toggleActivation(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        utilisateur.setActif(!utilisateur.isActif());
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        
        log.info("Activation utilisateur {} changée à: {}", 
                savedUtilisateur.getEmail(), savedUtilisateur.isActif());
        
        return mapToResponse(savedUtilisateur);
    }

    public List<UtilisateurResponse> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        log.info("Récupération de {} utilisateurs", utilisateurs.size());
        return utilisateurs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UtilisateurResponse> searchAndFilter(String keyword, Role role) {
        List<Utilisateur> utilisateurs;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Si keyword est fourni → recherche par mot-clé uniquement (ignore role)
            utilisateurs = utilisateurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrPosteContainingIgnoreCase(
                    keyword.trim(), keyword.trim(), keyword.trim());
            log.info("Recherche par mot-clé: {} résultats pour keyword='{}'", utilisateurs.size(), keyword);
        } else if (role != null) {
            // Si aucun keyword mais un role est fourni, filtrer par role
            utilisateurs = utilisateurRepository.findByRole(role);
            log.info("Filtrage par rôle: {} résultats pour role='{}'", utilisateurs.size(), role);
        } else {
            // Si ni keyword ni role, retourner tous les utilisateurs
            utilisateurs = utilisateurRepository.findAll();
            log.info("Aucun filtre: {} utilisateurs retournés", utilisateurs.size());
        }
        
        return utilisateurs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UtilisateurResponse mapToResponse(Utilisateur utilisateur) {
        UtilisateurResponse response = new UtilisateurResponse();
        response.setId(utilisateur.getId());
        response.setNom(utilisateur.getNom());
        response.setPrenom(utilisateur.getPrenom());
        response.setEmail(utilisateur.getEmail());
        response.setCIN(utilisateur.getCIN());
        response.setNumeroTel(utilisateur.getNumeroTel());
        response.setPoste(utilisateur.getPoste());
        response.setCompetences(utilisateur.getCompetences());
        response.setRole(utilisateur.getRole().name());
        response.setTypeUtilisateur(utilisateur.getTypeUtilisateur());
        response.setActif(utilisateur.isActif());
        response.setPhoto(utilisateur.getPhoto());
        return response;
    }
}
