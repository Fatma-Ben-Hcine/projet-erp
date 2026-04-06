package com.projet.security;

import com.projet.entity.Utilisateur;
import com.projet.exception.AccountDisabledException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.info("Tentative d'authentification pour l'email: {}", email);

        try {
            Utilisateur utilisateur = (Utilisateur) userDetailsService.loadUserByUsername(email);
            
            // Vérifier si le compte est désactivé AVANT de vérifier le mot de passe
            if (!utilisateur.isEnabled()) {
                log.warn("Tentative de connexion avec un compte désactivé: {}", email);
                throw new AccountDisabledException("Employé désactivé pour le moment");
            }

            // Vérifier le mot de passe
            if (!passwordEncoder.matches(password, utilisateur.getPassword())) {
                log.warn("Mot de passe incorrect pour l'email: {}", email);
                throw new BadCredentialsException("Email ou mot de passe incorrect");
            }

            log.info("Authentification réussie pour l'utilisateur: {}", email);
            
            // Retourner l'authentification avec les autorités
            return new UsernamePasswordAuthenticationToken(
                    utilisateur, 
                    password, 
                    utilisateur.getAuthorities()
            );

        } catch (AccountDisabledException e) {
            throw e; // Relancer notre exception personnalisée
        } catch (BadCredentialsException e) {
            throw e; // Relancer l'exception de mauvais mot de passe
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification pour {}: {}", email, e.getMessage());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
