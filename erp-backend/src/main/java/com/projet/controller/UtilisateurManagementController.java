package com.projet.controller;

import com.projet.dto.CreateUtilisateurRequest;
import com.projet.dto.UpdateUtilisateurRequest;
import com.projet.dto.UtilisateurResponse;
import com.projet.enums.Role;
import com.projet.enums.TypeUtilisateur;
import com.projet.exception.EntityNotFoundException;
import com.projet.service.FileUploadService;
import com.projet.service.UtilisateurManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UtilisateurManagementController {

    private final UtilisateurManagementService utilisateurManagementService;
    private final FileUploadService fileUploadService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UtilisateurResponse>> getAllUtilisateurs() {
        List<UtilisateurResponse> utilisateurs = utilisateurManagementService.getAllUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UtilisateurResponse>> searchAndFilter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Role roleEnum = null;
        if (role != null) {
            roleEnum = "ADMIN".equals(role) ? Role.ROLE_ADMIN : Role.ROLE_EMPLOYE;
        }
        
        List<UtilisateurResponse> utilisateurs = utilisateurManagementService.searchAndFilter(keyword, roleEnum);
        return ResponseEntity.ok(utilisateurs);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UtilisateurResponse> createUtilisateur(@Valid @RequestBody CreateUtilisateurRequest request) {
        log.info("Requête de création utilisateur reçue: {}", request);
        UtilisateurResponse response = utilisateurManagementService.createUtilisateur(request);
        log.info("Utilisateur créé avec succès: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload-photo", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestPart("photo") MultipartFile photoFile) {
        try {
            // Valider le fichier
            if (photoFile == null || photoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun fichier fourni"));
            }

            // Valider la taille (2MB max)
            if (photoFile.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "La photo ne doit pas dépasser 2MB."));
            }

            // Valider le format
            if (!fileUploadService.isValidPhotoFile(photoFile)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Format non accepté"));
            }

            // Upload et retourner l'URL
            String photoUrl = fileUploadService.uploadPhoto(photoFile);
            System.out.println("Returning photoUrl: " + photoUrl);
            return ResponseEntity.ok(Map.of("photoUrl", photoUrl));

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de la photo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de l'enregistrement de la photo"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateUtilisateurRequest request) {
        UtilisateurResponse response = utilisateurManagementService.updateUtilisateur(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUtilisateur(@PathVariable Long id) {
        try {
            // Empêcher un admin de supprimer son propre compte
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            var utilisateur = utilisateurManagementService.getAllUtilisateurs().stream()
                    .filter(u -> u.getId().equals(id) && u.getEmail().equals(currentUserEmail))
                    .findFirst();
            
            if (utilisateur.isPresent() && utilisateur.get().getRole().equals(Role.ROLE_ADMIN)) {
                return ResponseEntity.badRequest().body("Un administrateur ne peut pas supprimer son propre compte");
            }
            
            String message = utilisateurManagementService.deleteUtilisateur(id);
            return ResponseEntity.ok(message);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression de l'utilisateur");
        }
    }

    @PatchMapping("/{id}/toggle-activation")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UtilisateurResponse> toggleActivation(@PathVariable Long id) {
        try {
            // Empêcher un admin de désactiver son propre compte
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            
            var utilisateur = utilisateurManagementService.getAllUtilisateurs().stream()
                    .filter(u -> u.getId().equals(id) && u.getEmail().equals(currentUserEmail))
                    .findFirst();
            
            if (utilisateur.isPresent() && utilisateur.get().getRole().equals(Role.ROLE_ADMIN)) {
                return ResponseEntity.badRequest().build();
            }
            
            UtilisateurResponse response = utilisateurManagementService.toggleActivation(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
