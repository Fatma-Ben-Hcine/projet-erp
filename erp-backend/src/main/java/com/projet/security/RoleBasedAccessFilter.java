package com.projet.security;

import com.projet.exception.AccessDeniedException;
import com.projet.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@Slf4j
public class RoleBasedAccessFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("RoleBasedAccessFilter - Path: {}, Method: {}", path, method);
        
        // Vérifier si c'est une route admin
        if (path.startsWith("/api/admin/")) {
            Authentication authentication = getAuthentication();
            
            log.debug("RoleBasedAccessFilter - Authentication: {}", authentication);
            
            if (authentication == null) {
                log.warn("Accès admin refusé - Authentication null pour: {} {}", method, path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"erreur\":\"Authentification requise\",\"statut\":403}");
                response.getWriter().flush();
                return;
            }
            
            if (!authentication.isAuthenticated()) {
                log.warn("Accès admin refusé - Non authentifié pour: {} {}", method, path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"erreur\":\"Non authentifié\",\"statut\":403}");
                response.getWriter().flush();
                return;
            }
            
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            log.debug("RoleBasedAccessFilter - Authorities: {}", authorities);
            
            boolean isAdmin = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            log.debug("RoleBasedAccessFilter - IsAdmin: {}", isAdmin);
            
            if (!isAdmin) {
                String userEmail = authentication.getName();
                log.warn("Employé {} a tenté d'accéder à une opération admin: {} {}", userEmail, method, path);
                
                // Retourner une réponse JSON directement au lieu de lancer une exception
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                
                String jsonResponse = String.format(
                    "{\"erreur\":\"Accès refusé : Les opérations d'administration sont réservées aux administrateurs. Employé : %s\",\"statut\":403}", 
                    userEmail
                );
                
                response.getWriter().write(jsonResponse);
                response.getWriter().flush();
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Authentication getAuthentication() {
        // Récupérer l'authentication depuis le SecurityContext
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Ne pas filtrer les endpoints publics ou d'authentification
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/user/") ||
               path.startsWith("/api/employe/") ||
               !path.startsWith("/api/");
    }
}
