package com.projet.controller;

import com.projet.entity.Depot;
import com.projet.repository.DepotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/admin/depots")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class DepotController {

    private final DepotRepository depotRepository;

    @GetMapping("/{id}/telecharger")
    public ResponseEntity<Resource> telechargerDepot(@PathVariable Long id) {
        log.info("Téléchargement du dépôt {}", id);
        
        Depot depot = depotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépôt non trouvé avec l'id: " + id));
        
        try {
            Path filePath = Paths.get(depot.getCheminFichier());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                log.error("Fichier non trouvé: {}", depot.getCheminFichier());
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            log.info("Fichier trouvé: {}, type: {}", depot.getNomFichier(), contentType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + depot.getNomFichier() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du dépôt {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
