package com.projet.service;

import com.projet.dto.ClientResponse;
import com.projet.dto.ClientRequest;
import com.projet.entity.Client;
import com.projet.exception.DataChangeNotification;
import com.projet.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ClientService - Exemple d'intégration du refresh automatique
 * 
 * Toutes les méthodes CREATE/UPDATE/DELETE sont annotées avec @DataChangeNotification
 * Ce qui déclenche automatiquement:
 * 1. Mise à jour de la version dans DataVersionService
 * 2. Notification WebSocket aux clients Angular
 * 3. Rafraîchissement du dashboard Power BI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceExample {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    /**
     * Créer un nouveau client
     * ✨ Déclenche automatiquement refresh du dashboard
     */
    @DataChangeNotification("dashboard_main")
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        log.info("📝 Création d'un nouveau client: {}", request.getNom());
        
        // Validation
        if (request.getNom() == null || request.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du client est requis");
        }

        // Créer l'entité
        Client client = Client.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .build();

        // Sauvegarder
        Client saved = clientRepository.save(client);
        log.info("✅ Client créé avec ID: {}", saved.getId());
        
        // ⬅️ À ce moment, l'aspect @DataChangeNotification:
        //   1. Crée une nouvelle version
        //   2. Envoie notification WebSocket
        //   3. Angular recharge l'iframe Power BI
        
        return clientMapper.toResponse(saved);
    }

    /**
     * Modifier un client existant
     * ✨ Déclenche automatiquement refresh du dashboard
     */
    @DataChangeNotification("dashboard_main")
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        log.info("✏️ Modification du client ID: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé: " + id));

        // Mettre à jour les champs
        if (request.getNom() != null) {
            client.setNom(request.getNom());
        }
        if (request.getEmail() != null) {
            client.setEmail(request.getEmail());
        }
        if (request.getTelephone() != null) {
            client.setTelephone(request.getTelephone());
        }
        if (request.getAdresse() != null) {
            client.setAdresse(request.getAdresse());
        }

        Client updated = clientRepository.save(client);
        log.info("✅ Client mis à jour: {}", id);
        
        // ⬅️ L'aspect déclenche le refresh automatiquement
        
        return clientMapper.toResponse(updated);
    }

    /**
     * Supprimer un client
     * ✨ Déclenche automatiquement refresh du dashboard
     */
    @DataChangeNotification("dashboard_main")
    @Transactional
    public void deleteClient(Long id) {
        log.info("🗑️ Suppression du client ID: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé: " + id));

        clientRepository.delete(client);
        log.info("✅ Client supprimé: {}", id);
        
        // ⬅️ L'aspect déclenche le refresh automatiquement
    }

    /**
     * Récupérer tous les clients (lecture, sans refresh)
     * ⚠️ PAS d'annotation car c'est une lecture
     */
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un client par ID (lecture, sans refresh)
     * ⚠️ PAS d'annotation car c'est une lecture
     */
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé: " + id));
        return clientMapper.toResponse(client);
    }
}
