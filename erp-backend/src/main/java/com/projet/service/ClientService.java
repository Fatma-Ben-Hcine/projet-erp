package com.projet.service;

import com.projet.dto.ClientRequest;
import com.projet.dto.ClientResponse;
import com.projet.entity.Client;
import com.projet.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id: " + id));
        return mapToResponse(client);
    }

    public List<ClientResponse> searchClients(String keyword) {
        return clientRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
                    keyword, keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un client avec cet email existe déjà");
        }
        if (request.getMatriculeFiscale() != null &&
            clientRepository.existsByMatriculeFiscale(request.getMatriculeFiscale())) {
            throw new RuntimeException("Un client avec cette matricule fiscale existe déjà");
        }

        Client client = new Client();
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setEmail(request.getEmail());
        client.setNumeroTelephone(request.getNumeroTelephone());
        client.setMatriculeFiscale(request.getMatriculeFiscale());

        Client saved = clientRepository.save(client);
        log.info("Client créé: {}", saved.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id: " + id));

        if (!client.getEmail().equals(request.getEmail()) &&
            clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un client avec cet email existe déjà");
        }

        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setEmail(request.getEmail());
        client.setNumeroTelephone(request.getNumeroTelephone());
        client.setMatriculeFiscale(request.getMatriculeFiscale());

        Client updated = clientRepository.save(client);
        log.info("Client mis à jour: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id: " + id));
        clientRepository.delete(client);
        log.info("Client supprimé: {}", id);
    }

    private ClientResponse mapToResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setNom(client.getNom());
        response.setPrenom(client.getPrenom());
        response.setEmail(client.getEmail());
        response.setNumeroTelephone(client.getNumeroTelephone());
        response.setMatriculeFiscale(client.getMatriculeFiscale());
        response.setNombreContrats(
            client.getContrats() != null ? client.getContrats().size() : 0
        );
        return response;
    }
}
