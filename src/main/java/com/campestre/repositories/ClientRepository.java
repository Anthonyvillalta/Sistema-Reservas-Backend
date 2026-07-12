package com.campestre.repositories;

import com.campestre.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    @Query("SELECT c FROM Client c WHERE " +
           "(:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:celular IS NULL OR c.celular LIKE CONCAT('%', :celular, '%'))")
    List<Client> search(@Param("nombre") String nombre,
                        @Param("email") String email,
                        @Param("celular") String celular);
}
