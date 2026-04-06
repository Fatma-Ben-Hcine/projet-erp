package com.projet.service;

import com.projet.dto.UserProfileDTO;
import com.projet.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    public UserProfileDTO getAuthenticatedUserProfile() {
        // Extraire l'utilisateur authentifié du contexte de sécurité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // Le principal est l'objet UserDetails retourné par notre UserDetailsService
        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof Utilisateur)) {
            throw new RuntimeException("Type d'utilisateur non valide");
        }

        Utilisateur utilisateur = (Utilisateur) principal;
        
        // Mapper les champs vers le DTO
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setNom(utilisateur.getNom());
        userProfileDTO.setPrenom(utilisateur.getPrenom());
        userProfileDTO.setEmail(utilisateur.getEmail());
        userProfileDTO.setCIN(utilisateur.getCIN());
        userProfileDTO.setNumeroTel(utilisateur.getNumeroTel());
        userProfileDTO.setPoste(utilisateur.getPoste());
        userProfileDTO.setCompetences(utilisateur.getCompetences());
        userProfileDTO.setRole(utilisateur.getRole().name());
        userProfileDTO.setPhoto(utilisateur.getPhoto());
        
        log.info("Profil utilisateur récupéré pour: {}", utilisateur.getEmail());
        return userProfileDTO;
    }
}
