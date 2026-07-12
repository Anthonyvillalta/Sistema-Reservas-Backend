package com.campestre.repositories;

import com.campestre.entities.Environment;
import com.campestre.enums.EnvironmentStatus;
import com.campestre.enums.EnvironmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    List<Environment> findByEstado(EnvironmentStatus estado);
    List<Environment> findByTipo(EnvironmentType tipo);
    List<Environment> findByTipoAndEstado(EnvironmentType tipo, EnvironmentStatus estado);
    boolean existsByNombre(String nombre);
}
