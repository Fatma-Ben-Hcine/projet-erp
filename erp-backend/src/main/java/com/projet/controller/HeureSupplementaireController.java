package com.projet.controller;

import com.projet.dto.HeureSupplementaireDTO;
import com.projet.dto.HeureSupplementaireRequest;
import com.projet.entity.StatutHeureSupplementaire;
import com.projet.service.HeureSupplementaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/heures-supplementaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class HeureSupplementaireController {

    private final HeureSupplementaireService heureSupplementaireService;

    @GetMapping
    public ResponseEntity<List<HeureSupplementaireDTO>> getAll() {
        return ResponseEntity.ok(heureSupplementaireService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeureSupplementaireDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(heureSupplementaireService.getById(id));
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<HeureSupplementaireDTO>> getByEmployeId(@PathVariable Long employeId) {
        return ResponseEntity.ok(heureSupplementaireService.getByEmployeId(employeId));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody HeureSupplementaireRequest request) {
        try {
            log.info("Creating heure supplementaire with request: {}", request);
            
            // Convertir le request en DTO
            HeureSupplementaireDTO dto = new HeureSupplementaireDTO();
            dto.setDate(request.getDate());
            dto.setNombreHeures(request.getNombreHeures());
            dto.setMission(request.getMission());
            dto.setStatut(request.getStatut());
            dto.setTarifHeuresSupp(request.getTarifHeuresSupp());
            dto.setEmployeId(request.getEmployeId());
            
            HeureSupplementaireDTO newHeureSupplementaire = heureSupplementaireService.create(dto);
            log.info("Successfully created heure supplementaire with ID: {}", newHeureSupplementaire.getId());
            return ResponseEntity.ok(newHeureSupplementaire);
        } catch (RuntimeException e) {
            log.error("Error creating heure supplementaire: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody HeureSupplementaireRequest request) {
        try {
            log.info("Updating heure supplementaire {} with request: {}", id, request);
            
            // Convertir le request en DTO
            HeureSupplementaireDTO dto = new HeureSupplementaireDTO();
            dto.setDate(request.getDate());
            dto.setNombreHeures(request.getNombreHeures());
            dto.setMission(request.getMission());
            dto.setStatut(request.getStatut());
            dto.setTarifHeuresSupp(request.getTarifHeuresSupp());
            dto.setEmployeId(request.getEmployeId());
            
            HeureSupplementaireDTO updatedHeureSupplementaire = heureSupplementaireService.update(id, dto);
            log.info("Successfully updated heure supplementaire {}", id);
            return ResponseEntity.ok(updatedHeureSupplementaire);
        } catch (RuntimeException e) {
            log.error("Error updating heure supplementaire {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            log.info("Deleting heure supplementaire with ID: {}", id);
            heureSupplementaireService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting heure supplementaire {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<HeureSupplementaireDTO>> getByStatut(@PathVariable StatutHeureSupplementaire statut) {
        return ResponseEntity.ok(heureSupplementaireService.getByStatut(statut));
    }

    @PutMapping("/{id}/approuver")
    public ResponseEntity<?> approuver(@PathVariable Long id) {
        try {
            HeureSupplementaireDTO updated = heureSupplementaireService.approuver(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/refuser")
    public ResponseEntity<?> refuser(@PathVariable Long id) {
        try {
            HeureSupplementaireDTO updated = heureSupplementaireService.refuser(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
