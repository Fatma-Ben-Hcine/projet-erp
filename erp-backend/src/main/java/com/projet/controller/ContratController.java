package com.projet.controller;

import com.projet.dto.ContratRequest;
import com.projet.dto.ContratResponse;
import com.projet.service.ContratService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contrats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContratController {

    private final ContratService contratService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ContratResponse>> getAllContrats() {
        return ResponseEntity.ok(contratService.getAllContrats());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ContratResponse> getContratById(
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(contratService.getContratById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ContratResponse>> getContratsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(contratService.getContratsByClient(clientId));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ContratResponse>> getContratsByStatut(
            @PathVariable String statut) {
        try {
            return ResponseEntity.ok(contratService.getContratsByStatut(statut));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createContrat(
            @Valid @RequestBody ContratRequest request) {
        try {
            ContratResponse response = contratService.createContrat(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateContrat(
            @PathVariable Long id,
            @Valid @RequestBody ContratRequest request) {
        try {
            ContratResponse response = contratService.updateContrat(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteContrat(@PathVariable Long id) {
        try {
            contratService.deleteContrat(id);
            return ResponseEntity.ok("Contrat supprimé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
