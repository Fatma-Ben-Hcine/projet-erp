package com.projet.controller;

import com.projet.dto.CongeDTO;
import com.projet.entity.StatutConge;
import com.projet.service.CongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CongeController {

    private final CongeService congeService;

    // Employee endpoints
    @PostMapping
    public ResponseEntity<?> creerConge(
            @RequestBody CongeDTO congeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        try {
            CongeDTO newConge = congeService.creerConge(congeDTO, email);
            return ResponseEntity.ok(newConge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierConge(
            @PathVariable Long id, 
            @RequestBody CongeDTO congeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        try {
            CongeDTO modifiedConge = congeService.modifierConge(id, congeDTO, email);
            return ResponseEntity.ok(modifiedConge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerConge(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String email = userDetails.getUsername();
        try {
            congeService.supprimerConge(id, email);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mes-conges")
    public ResponseEntity<?> getMesConges(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        try {
            List<CongeDTO> conges = congeService.getCongesByEmploye(email);
            return ResponseEntity.ok(conges);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<?> getAllConges() {
        try {
            List<CongeDTO> conges = congeService.getAllConges();
            return ResponseEntity.ok(conges);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<?> getCongesByStatut(@PathVariable StatutConge statut) {
        try {
            List<CongeDTO> conges = congeService.getCongesByStatut(statut);
            return ResponseEntity.ok(conges);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerConge(@PathVariable Long id) {
        try {
            CongeDTO validatedConge = congeService.validerConge(id);
            return ResponseEntity.ok(validatedConge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/refuser")
    public ResponseEntity<?> refuserConge(@PathVariable Long id) {
        try {
            CongeDTO refusedConge = congeService.refuserConge(id);
            return ResponseEntity.ok(refusedConge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/solde/{employeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYE')")
    public ResponseEntity<Map<String, Integer>> getSolde(@PathVariable Long employeId) {
        return ResponseEntity.ok(congeService.getSoldeDetails(employeId));
    }
}
