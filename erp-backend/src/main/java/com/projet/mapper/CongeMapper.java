package com.projet.mapper;

import com.projet.dto.CongeDTO;
import com.projet.entity.Conge;
import com.projet.entity.TypeConge;
import com.projet.entity.StatutConge;
import org.springframework.stereotype.Component;

@Component
public class CongeMapper {

    public CongeDTO toDTO(Conge conge) {
        if (conge == null) {
            return null;
        }

        CongeDTO dto = new CongeDTO();
        dto.setId(conge.getId());
        dto.setTypeConge(conge.getTypeConge());
        dto.setDateDebut(conge.getDateDebut());
        dto.setDateFin(conge.getDateFin());
        dto.setStatut(conge.getStatut());

        // Gérer les informations de l'employé
        if (conge.getEmploye() != null) {
            dto.setEmployeId(conge.getEmploye().getId());
            dto.setEmployeNom(conge.getEmploye().getNom());
            dto.setEmployePrenom(conge.getEmploye().getPrenom());
            dto.setEmployeNomComplet(
                conge.getEmploye().getPrenom() + " " + conge.getEmploye().getNom()
            );
        } else {
            dto.setEmployeNomComplet("Non spécifié");
        }

        return dto;
    }

    public Conge toEntity(CongeDTO dto) {
        if (dto == null) {
            return null;
        }

        Conge conge = new Conge();
        conge.setId(dto.getId());
        conge.setTypeConge(dto.getTypeConge());
        conge.setDateDebut(dto.getDateDebut());
        conge.setDateFin(dto.getDateFin());
        conge.setStatut(dto.getStatut());

        // Note: L'employé sera défini dans le service avec setEmploye()
        // car le DTO ne contient pas l'entité Employe complète

        return conge;
    }
}
