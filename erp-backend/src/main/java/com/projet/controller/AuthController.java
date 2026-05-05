package com.projet.controller;

import com.projet.dto.JwtResponse;
import com.projet.dto.LoginRequest;
import com.projet.dto.LogoutResponse;
import com.projet.entity.Utilisateur;
import com.projet.exception.AccountDisabledException;
import com.projet.security.JwtUtils;
import com.projet.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Tentative de connexion pour l'email: {}", loginRequest.getEmail());
        log.info("Requête reçue: {}", loginRequest);
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse())
            );

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            String jwt = jwtUtils.genererToken(authentication);
            
            // Récupérer l'ID de l'utilisateur
            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
            Long userId = utilisateur.getId();
            
            log.info("Connexion réussie pour l'utilisateur: {}", loginRequest.getEmail());
            log.info("Rôle de l'utilisateur: {}", role);
            log.info("ID de l'utilisateur: {}", userId);

            return ResponseEntity.ok(new JwtResponse(jwt, "Bearer", loginRequest.getEmail(), role, userId));
            
        } catch (AccountDisabledException e) {
            log.warn("Tentative de connexion avec un compte désactivé: {}", loginRequest.getEmail());
            return ResponseEntity.status(403).body("Employé désactivé pour le moment");
        } catch (BadCredentialsException e) {
            log.error("Erreur d'authentification pour l'email : {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur serveur lors de l'authentification");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        log.info("Tentative de déconnexion");
        
        try {
            String message = logoutService.logout(request);
            log.info("Déconnexion traitée: {}", message);
            
            // Retourner une réponse de succès
            return ResponseEntity.ok().body(new LogoutResponse(message));
            
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur serveur lors de la déconnexion");
        }
    }
}
