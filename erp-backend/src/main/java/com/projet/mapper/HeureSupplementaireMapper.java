package com.projet.mapper;

import com.projet.dto.HeureSupplementaireDTO;
import com.projet.entity.HeureSupplementaire;
import com.projet.entity.StatutHeureSupplementaire;
import org.springframework.stereotype.Component;

@Component
public class HeureSupplementaireMapper {

    public HeureSupplementaireDTO toDTO(HeureSupplementaire heureSupplementaire) {
        if (heureSupplementaire == null) {
            return null;
        }

        HeureSupplementaireDTO dto = new HeureSupplementaireDTO();
        dto.setId(heureSupplementaire.getId());
        dto.setDate(heureSupplementaire.getDate());
        dto.setNombreHeures(heureSupplementaire.getNombreHeures());
        dto.setMission(heureSupplementaire.getMission());
        dto.setStatut(heureSupplementaire.getStatut());
        dto.setTarifHeuresSupp(heureSupplementaire.getTarifHeuresSupp());

        // Gérer les informations de l'employé
        if (heureSupplementaire.getEmploye() != null) {
            dto.setEmployeId(heureSupplementaire.getEmploye().getId());
            dto.setEmployeNom(heureSupplementaire.getEmploye().getNom());
            dto.setEmployePrenom(heureSupplementaire.getEmploye().getPrenom());
            dto.setEmployeNomComplet(
                heureSupplementaire.getEmploye().getPrenom() + " " + heureSupplementaire.getEmploye().getNom()
            );
        } else {
            dto.setEmployeNomComplet("Non spécifié");
        }

        return dto;
    }

    public HeureSupplementaire toEntity(HeureSupplementaireDTO dto) {
        if (dto == null) {
            return null;
        }

        HeureSupplementaire heureSupplementaire = new HeureSupplementaire();
        heureSupplementaire.setId(dto.getId());
        heureSupplementaire.setDate(dto.getDate());
        heureSupplementaire.setNombreHeures(dto.getNombreHeures());
        heureSupplementaire.setMission(dto.getMission());
        heureSupplementaire.setStatut(dto.getStatut());
        heureSupplementaire.setTarifHeuresSupp(dto.getTarifHeuresSupp());

        // Note: L'employé sera défini dans le service avec setEmploye()
        // car le DTO ne contient pas l'entité Employe complète

        return heureSupplementaire;
    }
}
