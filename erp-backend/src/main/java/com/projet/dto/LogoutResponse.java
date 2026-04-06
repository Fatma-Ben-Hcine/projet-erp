package com.projet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {
    private String message;
    private boolean success = true;
    
    public LogoutResponse(String message) {
        this.message = message;
    }
}
