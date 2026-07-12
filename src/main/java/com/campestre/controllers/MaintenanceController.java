package com.campestre.controllers;

import com.campestre.dto.request.MaintenanceRequest;
import com.campestre.dto.response.MaintenanceResponse;
import com.campestre.entities.User;
import com.campestre.services.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mantenimientos")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceResponse>> findAll(
            @RequestParam(required = false) Long ambienteId,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(maintenanceService.findAll(ambienteId, estado));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaintenanceResponse> create(@Valid @RequestBody MaintenanceRequest request,
                                                       @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceService.create(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaintenanceResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        maintenanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
