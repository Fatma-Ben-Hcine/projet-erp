package com.projet.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContratResponse {
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal montant;
    private String statut;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;
}
