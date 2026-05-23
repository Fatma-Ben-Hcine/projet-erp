package com.projet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DashboardVersionController - Ultra simple
 * Retourne SEULEMENT le timestamp de la dernière modification
 */
@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardRefreshController {

    /**
     * Endpoint simple: retourne timestamp de dernière modification
     * Le client Angular poll cet endpoint toutes les 60 secondes
     * Si le timestamp change → refresh l'iframe Power BI
     */
    @GetMapping("/version")
    public ResponseEntity<Long> getDashboardVersion() {
        long timestamp = System.currentTimeMillis() / 1000; // en secondes
        log.debug("Dashboard version check: {}", timestamp);
        return ResponseEntity.ok(timestamp);
    }
}
