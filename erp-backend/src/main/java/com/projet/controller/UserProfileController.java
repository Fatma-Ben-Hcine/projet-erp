package com.projet.controller;

import com.projet.dto.UserProfileDTO;
import com.projet.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        try {
            UserProfileDTO userProfile = userProfileService.getAuthenticatedUserProfile();
            return ResponseEntity.ok(userProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(null);
        }
    }
}
