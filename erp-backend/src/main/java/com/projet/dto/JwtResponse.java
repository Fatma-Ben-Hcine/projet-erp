package com.projet.dto;

import lombok.Data;

@Data
public class JwtResponse {

    private String token;
    private String type;
    private String email;
    private String role;

    public JwtResponse(String token, String type, String email, String role) {
        this.token = token;
        this.type = type;
        this.email = email;
        this.role = role;
    }
}
