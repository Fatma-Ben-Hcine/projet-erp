package com.projet.service;

import com.projet.dto.CongeDTO;
import com.projet.entity.Conge;
import com.projet.entity.Employe;
import com.projet.entity.StatutConge;
import com.projet.mapper.CongeMapper;
import com.projet.repository.CongeRepository;
import com.projet.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final CongeMapper congeMapper;

    public CongeDTO creerConge(CongeDTO congeDTO, String email) {
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'email: " + email));

        // Calculer la durée du congé
        int duree = (int) ChronoUnit.DAYS.between(congeDTO.getDateDebut(), congeDTO.getDateFin()) + 1;

        // Vérifier le solde restant
        int soldeRestant = getSoldeRestant(employe.getId());
        if (duree > soldeRestant) {
            throw new RuntimeException(
                "Solde insuffisant. Solde restant: " + soldeRestant + " jours."
            );
        }

        Conge conge = congeMapper.toEntity(congeDTO);
        conge.setEmploye(employe);
        conge.setStatut(StatutConge.EN_ATTENTE);

        Conge saved = congeRepository.save(conge);
        return congeMapper.toDTO(saved);
    }

    public CongeDTO modifierConge(Long congeId, CongeDTO congeDTO, String email) {
        Conge existingConge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé avec l'ID: " + congeId));

        // Vérifier que le congé appartient à l'employé connecté
        if (!existingConge.getEmploye().getEmail().equals(email)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce congé");
        }

        // Calculer la nouvelle durée du congé
        int nouvelleDuree = (int) ChronoUnit.DAYS.between(congeDTO.getDateDebut(), congeDTO.getDateFin()) + 1;

        // Obtenir le total des jours déjà pris pour l'année courante
        int totalJoursPris = getTotalJoursPrisDeLAnneeCourante(existingConge.getEmploye().getId());

        // Soustraire les jours du congé actuel s'il est déjà validé (pour éviter le double comptage)
        int joursDejaComptes = 0;
        if (existingConge.getStatut() == StatutConge.VALIDE) {
            int anneeCourante = java.time.LocalDate.now().getYear();
            joursDejaComptes = getJoursPrisDansAnnee(existingConge, anneeCourante);
        }

        // Calculer le solde disponible pour cette modification
        int soldeDisponible = SOLDE_ANNUEL - (totalJoursPris - joursDejaComptes);

        // Vérifier si la nouvelle durée dépasse le solde disponible
        if (nouvelleDuree > soldeDisponible) {
            throw new RuntimeException(
                "Solde insuffisant. Solde disponible pour cette modification : " + soldeDisponible + " jours."
            );
        }

        existingConge.setTypeConge(congeDTO.getTypeConge());
        existingConge.setDateDebut(congeDTO.getDateDebut());
        existingConge.setDateFin(congeDTO.getDateFin());

        Conge saved = congeRepository.save(existingConge);
        return congeMapper.toDTO(saved);
    }

    public void supprimerConge(Long congeId, String email) {
        Conge existingConge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé avec l'ID: " + congeId));
        
        // Vérifier que le congé appartient à l'employé connecté
        if (!existingConge.getEmploye().getEmail().equals(email)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce congé");
        }
        
        if (existingConge.getStatut() != StatutConge.EN_ATTENTE) {
            throw new RuntimeException("Suppression impossible : la demande n'est plus en attente");
        }
        
        congeRepository.delete(existingConge);
    }

    public List<CongeDTO> getCongesByEmploye(String email) {
        Employe employe = employeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'email: " + email));
        
        List<Conge> conges = congeRepository.findByEmployeId(employe.getId());
        return conges.stream()
                .map(congeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CongeDTO validerConge(Long congeId) {
        Conge existingConge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé avec l'ID: " + congeId));
        
        existingConge.setStatut(StatutConge.VALIDE);
        Conge saved = congeRepository.save(existingConge);
        return congeMapper.toDTO(saved);
    }

    public CongeDTO refuserConge(Long congeId) {
        Conge existingConge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé avec l'ID: " + congeId));
        
        existingConge.setStatut(StatutConge.REFUSE);
        Conge saved = congeRepository.save(existingConge);
        return congeMapper.toDTO(saved);
    }

    public List<CongeDTO> getAllConges() {
        List<Conge> conges = congeRepository.findAllWithEmploye();
        return conges.stream()
                .map(congeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CongeDTO> getCongesByStatut(StatutConge statut) {
        List<Conge> conges = congeRepository.findByStatut(statut);
        return conges.stream()
                .map(congeMapper::toDTO)
                .collect(Collectors.toList());
    }

    private static final int SOLDE_ANNUEL = 21;

    public int getSoldeRestant(Long employeId) {
        int joursPris = getTotalJoursPrisDeLAnneeCourante(employeId);
        return SOLDE_ANNUEL - joursPris;
    }

    private int getTotalJoursPrisDeLAnneeCourante(Long employeId) {
        int anneeCourante = java.time.LocalDate.now().getYear();
        List<Conge> valideConges = congeRepository.findValideCongesByEmployeId(employeId);
        return valideConges.stream()
                .mapToInt(c -> getJoursPrisDansAnnee(c, anneeCourante))
                .sum();
    }

    private int getJoursPrisDansAnnee(Conge conge, int annee) {
        java.time.LocalDate debutAnnee = java.time.LocalDate.of(annee, 1, 1);
        java.time.LocalDate finAnnee = java.time.LocalDate.of(annee, 12, 31);
        java.time.LocalDate debut = conge.getDateDebut().isBefore(debutAnnee)
                ? debutAnnee
                : conge.getDateDebut();
        java.time.LocalDate fin = conge.getDateFin().isAfter(finAnnee)
                ? finAnnee
                : conge.getDateFin();

        if (debut.isAfter(fin)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(debut, fin) + 1;
    }

    public Map<String, Integer> getSoldeDetails(Long employeId) {
        int joursPris = getTotalJoursPrisDeLAnneeCourante(employeId);
        int soldeTotal = SOLDE_ANNUEL;
        int soldeRestant = soldeTotal - joursPris;
        Map<String, Integer> details = new HashMap<>();
        details.put("soldeTotal", soldeTotal);
        details.put("joursPris", joursPris);
        details.put("soldeRestant", soldeRestant);
        return details;
    }
}
