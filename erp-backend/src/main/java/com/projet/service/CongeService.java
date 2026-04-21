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

import java.util.List;
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
        
        if (existingConge.getStatut() != StatutConge.EN_ATTENTE) {
            throw new RuntimeException("Modification impossible : la demande n'est plus en attente");
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
}
