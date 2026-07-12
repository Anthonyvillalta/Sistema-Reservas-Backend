package com.campestre.services;

import com.campestre.dto.request.EnvironmentRequest;
import com.campestre.dto.response.EnvironmentResponse;
import com.campestre.entities.Environment;
import com.campestre.enums.EnvironmentStatus;
import com.campestre.enums.EnvironmentType;
import com.campestre.exceptions.BusinessException;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.mappers.EnvironmentMapper;
import com.campestre.repositories.EnvironmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvironmentService {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentMapper environmentMapper;

    public EnvironmentService(EnvironmentRepository environmentRepository, EnvironmentMapper environmentMapper) {
        this.environmentRepository = environmentRepository;
        this.environmentMapper = environmentMapper;
    }

    @Transactional(readOnly = true)
    public List<EnvironmentResponse> findAll(String tipo, String estado) {
        List<Environment> environments;

        if (tipo != null && estado != null) {
            environments = environmentRepository.findByTipoAndEstado(
                    EnvironmentType.valueOf(tipo.toUpperCase()),
                    EnvironmentStatus.valueOf(estado.toUpperCase()));
        } else if (tipo != null) {
            environments = environmentRepository.findByTipo(EnvironmentType.valueOf(tipo.toUpperCase()));
        } else if (estado != null) {
            environments = environmentRepository.findByEstado(EnvironmentStatus.valueOf(estado.toUpperCase()));
        } else {
            environments = environmentRepository.findAll();
        }

        return environments.stream()
                .map(environmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public EnvironmentResponse findById(Long id) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", id));
        return environmentMapper.toResponse(environment);
    }

    @Transactional
    public EnvironmentResponse create(EnvironmentRequest request) {
        if (environmentRepository.existsByNombre(request.getNombre())) {
            throw new BusinessException("Ya existe un ambiente con el nombre: " + request.getNombre());
        }

        Environment environment = Environment.builder()
                .nombre(request.getNombre())
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion())
                .precioBase(request.getPrecioBase())
                .capacidadMaxima(request.getCapacidadMaxima())
                .estado(request.getEstado() != null ? request.getEstado() : EnvironmentStatus.ACTIVO)
                .imagenUrl(request.getImagenUrl())
                .build();

        environment = environmentRepository.save(environment);
        log.info("Ambiente creado: {} - {}", environment.getId(), environment.getNombre());

        return environmentMapper.toResponse(environment);
    }

    @Transactional
    public EnvironmentResponse update(Long id, EnvironmentRequest request) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", id));

        if (!environment.getNombre().equals(request.getNombre()) &&
                environmentRepository.existsByNombre(request.getNombre())) {
            throw new BusinessException("Ya existe un ambiente con el nombre: " + request.getNombre());
        }

        environment.setNombre(request.getNombre());
        environment.setTipo(request.getTipo());
        environment.setDescripcion(request.getDescripcion());
        environment.setPrecioBase(request.getPrecioBase());
        environment.setCapacidadMaxima(request.getCapacidadMaxima());
        if (request.getEstado() != null) {
            environment.setEstado(request.getEstado());
        }
        environment.setImagenUrl(request.getImagenUrl());

        environment = environmentRepository.save(environment);
        log.info("Ambiente actualizado: {} - {}", environment.getId(), environment.getNombre());

        return environmentMapper.toResponse(environment);
    }

    @Transactional
    public void delete(Long id) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", id));

        if (environment.getReservations() != null && !environment.getReservations().isEmpty()) {
            throw new BusinessException("No se puede eliminar el ambiente porque tiene reservas asociadas");
        }

        environmentRepository.delete(environment);
        log.info("Ambiente eliminado: {}", id);
    }

    @Transactional
    public EnvironmentResponse updateStatus(Long id, String estado) {
        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente", id));

        environment.setEstado(EnvironmentStatus.valueOf(estado.toUpperCase()));
        environment = environmentRepository.save(environment);
        log.info("Estado del ambiente {} actualizado a: {}", id, estado);

        return environmentMapper.toResponse(environment);
    }
}
