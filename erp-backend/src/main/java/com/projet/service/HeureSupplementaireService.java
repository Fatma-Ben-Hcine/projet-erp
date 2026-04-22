package com.projet.service;

import com.projet.dto.HeureSupplementaireDTO;
import com.projet.entity.HeureSupplementaire;
import com.projet.entity.Employe;
import com.projet.entity.StatutHeureSupplementaire;
import com.projet.mapper.HeureSupplementaireMapper;
import com.projet.repository.HeureSupplementaireRepository;
import com.projet.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HeureSupplementaireService {

    private final HeureSupplementaireRepository heureSupplementaireRepository;
    private final EmployeRepository employeRepository;
    private final HeureSupplementaireMapper heureSupplementaireMapper;

    public List<HeureSupplementaireDTO> getAll() {
        List<HeureSupplementaire> heuresSupplementaires = heureSupplementaireRepository.findAllWithEmploye();
        return heuresSupplementaires.stream()
                .map(heureSupplementaireMapper::toDTO)
                .collect(Collectors.toList());
    }

    public HeureSupplementaireDTO getById(Long id) {
        HeureSupplementaire heureSupplementaire = heureSupplementaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Heure supplémentaire non trouvée avec l'ID: " + id));
        return heureSupplementaireMapper.toDTO(heureSupplementaire);
    }

    public HeureSupplementaireDTO create(HeureSupplementaireDTO dto) {
        // Vérifier que l'employé existe
        Employe employe = employeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + dto.getEmployeId()));

        HeureSupplementaire heureSupplementaire = heureSupplementaireMapper.toEntity(dto);
        heureSupplementaire.setEmploye(employe);
        heureSupplementaire.setStatut(StatutHeureSupplementaire.EN_ATTENTE);

        HeureSupplementaire saved = heureSupplementaireRepository.save(heureSupplementaire);
        return heureSupplementaireMapper.toDTO(saved);
    }

    public HeureSupplementaireDTO update(Long id, HeureSupplementaireDTO dto) {
        HeureSupplementaire existing = heureSupplementaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Heure supplémentaire non trouvée avec l'ID: " + id));

        // Mettre à jour les champs
        existing.setDate(dto.getDate());
        existing.setNombreHeures(dto.getNombreHeures());
        existing.setMission(dto.getMission());
        existing.setStatut(dto.getStatut());
        existing.setTarifHeuresSupp(dto.getTarifHeuresSupp());

        // Mettre à jour l'employé si nécessaire
        if (dto.getEmployeId() != null && !dto.getEmployeId().equals(existing.getEmploye().getId())) {
            Employe employe = employeRepository.findById(dto.getEmployeId())
                    .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + dto.getEmployeId()));
            existing.setEmploye(employe);
        }

        HeureSupplementaire updated = heureSupplementaireRepository.save(existing);
        return heureSupplementaireMapper.toDTO(updated);
    }

    public void delete(Long id) {
        if (!heureSupplementaireRepository.existsById(id)) {
            throw new RuntimeException("Heure supplémentaire non trouvée avec l'ID: " + id);
        }
        heureSupplementaireRepository.deleteById(id);
    }

    public List<HeureSupplementaireDTO> getByEmployeId(Long employeId) {
        List<HeureSupplementaire> heuresSupplementaires = heureSupplementaireRepository.findByEmployeId(employeId);
        return heuresSupplementaires.stream()
                .map(heureSupplementaireMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<HeureSupplementaireDTO> getByStatut(StatutHeureSupplementaire statut) {
        List<HeureSupplementaire> heuresSupplementaires = heureSupplementaireRepository.findByStatut(statut);
        return heuresSupplementaires.stream()
                .map(heureSupplementaireMapper::toDTO)
                .collect(Collectors.toList());
    }

    public HeureSupplementaireDTO approuver(Long id) {
        HeureSupplementaire existing = heureSupplementaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Heure supplémentaire non trouvée avec l'ID: " + id));
        
        existing.setStatut(StatutHeureSupplementaire.APPROUVEE);
        HeureSupplementaire saved = heureSupplementaireRepository.save(existing);
        return heureSupplementaireMapper.toDTO(saved);
    }

    public HeureSupplementaireDTO refuser(Long id) {
        HeureSupplementaire existing = heureSupplementaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Heure supplémentaire non trouvée avec l'ID: " + id));
        
        existing.setStatut(StatutHeureSupplementaire.REFUSEE);
        HeureSupplementaire saved = heureSupplementaireRepository.save(existing);
        return heureSupplementaireMapper.toDTO(saved);
    }
}
