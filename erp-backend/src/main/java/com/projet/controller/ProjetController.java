package com.projet.controller;

import com.projet.dto.ProjetRequest;
import com.projet.dto.ProjetResponse;
import com.projet.dto.DepotRequest;
import com.projet.enums.StatutProjet;
import com.projet.service.ProjetService;
import com.projet.service.ProjetProgressionService;
import com.projet.repository.ProjetRepository;
import com.projet.entity.Projet;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/projets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjetController {

    private final ProjetService projetService;
    private final ProjetProgressionService projetProgressionService;
    private final ProjetRepository projetRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ProjetResponse>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAllProjets());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProjetResponse> getProjetById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(projetService.getProjetById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ProjetResponse>> searchProjets(
            @RequestParam String keyword) {
        return ResponseEntity.ok(projetService.searchProjets(keyword));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createProjet(
            @Valid @RequestBody ProjetRequest request) {
        try {
            ProjetResponse response = projetService.createProjet(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateProjet(
            @PathVariable Long id,
            @Valid @RequestBody ProjetRequest request) {
        try {
            ProjetResponse response = projetService.updateProjet(id, request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.ok("Projet supprimé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateProjetStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String statutValue = request.get("statut");
            StatutProjet statut = StatutProjet.valueOf(statutValue.toUpperCase());
            ProjetResponse response = projetService.updateProjetStatut(id, statut);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/depot-exists")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkDepotExists(@PathVariable Long id) {
        log.info("GET /api/admin/projets/{}/depot-exists - Vérification dépôt projet", id);
        try {
            boolean exists = projetService.hasDepot(id);
            Map<String, Object> response = Map.of(
                "hasDepot", exists,
                "projetId", id
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du dépôt: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/toutes-activites-deposees")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkAllActivitesDeposees(@PathVariable Long id) {
        log.info("GET /api/admin/projets/{}/toutes-activites-deposees - Vérification activités déposées", id);
        try {
            boolean allDeposees = projetService.areAllActivitesDeposees(id);
            Map<String, Object> response = Map.of(
                "allDeposees", allDeposees,
                "projetId", id
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des activités: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping(value = "/{id}/depot", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deposerProjet(
            @PathVariable Long id,
            @RequestPart("type") String type,
            @RequestPart(value = "lien", required = false) String lien,
            @RequestPart(value = "nomFichier", required = false) String nomFichier,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            System.out.println("=== DEBUG DEPOT REQUEST ===");
            System.out.println("ID Projet: " + id);
            System.out.println("Type: " + type);
            System.out.println("Lien: " + lien);
            System.out.println("NomFichier: " + nomFichier);
            System.out.println("File: " + (file != null ? file.getOriginalFilename() + " (" + file.getSize() + " bytes)" : "null"));
            System.out.println("===========================");

            DepotRequest depotRequest = new DepotRequest();
            depotRequest.setType(type);
            depotRequest.setLien(lien);
            depotRequest.setNomFichier(nomFichier);
            depotRequest.setCheminFichier(null);

            ProjetResponse response = projetService.deposerProjet(id, depotRequest, file);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Erreur RuntimeException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur lors du stockage du fichier: " + e.getMessage());
        }
    }

    @GetMapping("/depots/{id}/download")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Resource> downloadDepotFile(@PathVariable Long id) {
        try {
            String filePath = projetService.getDepotFilePath(id);
            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String filename = path.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint admin one-shot pour corriger les progressions des projets existants.
     * À appeler une seule fois puis à supprimer ou sécuriser.
     */
    @GetMapping("/admin/fix-progressions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> fixProgressions() {
        List<Projet> projets = projetRepository.findAll();
        int count = 0;
        for (Projet projet : projets) {
            projetProgressionService.recalculerEtSauvegarder(projet.getId());
            count++;
        }
        log.info("Progressions recalculées pour {} projets", count);
        return ResponseEntity.ok("Progressions recalculées : " + count + " projets");
    }
}
