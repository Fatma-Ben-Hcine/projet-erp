package com.projet.service;

import com.projet.dto.ContratRequest;
import com.projet.dto.ContratResponse;
import com.projet.entity.Client;
import com.projet.entity.Contrat;
import com.projet.enums.StatutContrat;
import com.projet.repository.ClientRepository;
import com.projet.repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContratService {

    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;

    public List<ContratResponse> getAllContrats() {
        return contratRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ContratResponse getContratById(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Contrat non trouvé avec l'id: " + id));
        return mapToResponse(contrat);
    }

    public List<ContratResponse> getContratsByClient(Long clientId) {
        return contratRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ContratResponse> getContratsByStatut(String statut) {
        StatutContrat statutContrat = StatutContrat.valueOf(statut);
        return contratRepository.findByStatut(statutContrat)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContratResponse createContrat(ContratRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException(
                    "Client non trouvé avec l'id: " + request.getClientId()));

        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new RuntimeException(
                "La date de fin doit être après la date de début");
        }

        Contrat contrat = new Contrat();
        contrat.setDateDebut(request.getDateDebut());
        contrat.setDateFin(request.getDateFin());
        contrat.setMontant(request.getMontant());
        contrat.setStatut(StatutContrat.valueOf(request.getStatut()));
        contrat.setClient(client);

        Contrat saved = contratRepository.save(contrat);
        log.info("Contrat créé: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public ContratResponse updateContrat(Long id, ContratRequest request) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Contrat non trouvé avec l'id: " + id));

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException(
                    "Client non trouvé avec l'id: " + request.getClientId()));

        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new RuntimeException(
                "La date de fin doit être après la date de début");
        }

        contrat.setDateDebut(request.getDateDebut());
        contrat.setDateFin(request.getDateFin());
        contrat.setMontant(request.getMontant());
        contrat.setStatut(StatutContrat.valueOf(request.getStatut()));
        contrat.setClient(client);

        Contrat updated = contratRepository.save(contrat);
        log.info("Contrat mis à jour: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Contrat non trouvé avec l'id: " + id));
        contratRepository.delete(contrat);
        log.info("Contrat supprimé: {}", id);
    }

    private ContratResponse mapToResponse(Contrat contrat) {
        ContratResponse response = new ContratResponse();
        response.setId(contrat.getId());
        response.setDateDebut(contrat.getDateDebut());
        response.setDateFin(contrat.getDateFin());
        response.setMontant(contrat.getMontant());
        response.setStatut(contrat.getStatut().name());
        response.setClientId(contrat.getClient().getId());
        response.setClientNom(contrat.getClient().getNom());
        response.setClientPrenom(contrat.getClient().getPrenom());
        response.setClientEmail(contrat.getClient().getEmail());
        return response;
    }
}
