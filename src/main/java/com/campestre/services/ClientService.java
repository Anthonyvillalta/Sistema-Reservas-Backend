package com.campestre.services;

import com.campestre.dto.request.ClientRequest;
import com.campestre.dto.response.ClientResponse;
import com.campestre.entities.Client;
import com.campestre.exceptions.BusinessException;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.mappers.ClientMapper;
import com.campestre.repositories.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll(String nombre, String email, String celular) {
        List<Client> clients = clientRepository.search(nombre, email, celular);
        return clients.stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return clientMapper.toResponse(client);
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        if (request.getDocumentoIdentidad() != null &&
                clientRepository.existsByDocumentoIdentidad(request.getDocumentoIdentidad())) {
            throw new BusinessException("Ya existe un cliente con el documento: " + request.getDocumentoIdentidad());
        }

        Client client = Client.builder()
                .nombre(request.getNombre())
                .celular(request.getCelular())
                .email(request.getEmail())
                .documentoIdentidad(request.getDocumentoIdentidad())
                .direccion(request.getDireccion())
                .build();

        client = clientRepository.save(client);
        log.info("Cliente creado: {} - {}", client.getId(), client.getNombre());

        return clientMapper.toResponse(client);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));

        client.setNombre(request.getNombre());
        client.setCelular(request.getCelular());
        client.setEmail(request.getEmail());
        client.setDireccion(request.getDireccion());

        if (request.getDocumentoIdentidad() != null) {
            client.setDocumentoIdentidad(request.getDocumentoIdentidad());
        }

        client = clientRepository.save(client);
        log.info("Cliente actualizado: {} - {}", client.getId(), client.getNombre());

        return clientMapper.toResponse(client);
    }

    @Transactional
    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));

        if (client.getReservations() != null && !client.getReservations().isEmpty()) {
            throw new BusinessException("No se puede eliminar el cliente porque tiene reservas asociadas");
        }

        clientRepository.delete(client);
        log.info("Cliente eliminado: {}", id);
    }
}
