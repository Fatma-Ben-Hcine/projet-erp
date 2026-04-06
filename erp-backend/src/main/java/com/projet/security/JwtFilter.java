package com.projet.security;

import com.projet.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        log.info("JwtFilter - Requête: {} {}", request.getMethod(), requestPath);
        
        try {
            String jwt = parseJwt(request);
            log.info("JwtFilter - Token extrait: {}", jwt != null ? "présent" : "absent");
            
            if (jwt != null) {
                if (!jwtUtils.validerToken(jwt)) {
                    String errorMessage = jwtUtils.getErrorMessage(jwt);
                    log.warn("Token invalide: {}", errorMessage);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(String.format("{\"erreur\":\"%s\",\"statut\":401}", errorMessage));
                    return;
                }
                
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    log.warn("Token blacklisté");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"erreur\":\"Token invalide\",\"statut\":401}");
                    return;
                }
                
                String email = jwtUtils.extraireEmail(jwt);
                log.info("JwtFilter - Email: {}", email);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.info("JwtFilter - User: {}, Authorities: {}", userDetails.getUsername(), userDetails.getAuthorities());
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JwtFilter - Auth définie");
            } else {
                log.warn("JwtFilter - Aucun token");
            }
        } catch (Exception e) {
            log.error("Erreur auth: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"erreur\":\"Token invalide\",\"statut\":401}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
}
