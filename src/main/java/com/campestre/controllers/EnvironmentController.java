package com.campestre.controllers;

import com.campestre.dto.request.EnvironmentRequest;
import com.campestre.dto.response.EnvironmentResponse;
import com.campestre.services.EnvironmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ambientes")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    public ResponseEntity<List<EnvironmentResponse>> findAll(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(environmentService.findAll(tipo, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(environmentService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnvironmentResponse> create(@Valid @RequestBody EnvironmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(environmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnvironmentResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody EnvironmentRequest request) {
        return ResponseEntity.ok(environmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        environmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnvironmentResponse> updateStatus(@PathVariable Long id,
                                                             @RequestBody String estado) {
        return ResponseEntity.ok(environmentService.updateStatus(id, estado));
    }
}
