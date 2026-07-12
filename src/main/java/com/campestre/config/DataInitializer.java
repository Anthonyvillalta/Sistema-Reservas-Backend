package com.campestre.config;

import com.campestre.entities.Environment;
import com.campestre.entities.User;
import com.campestre.enums.EnvironmentStatus;
import com.campestre.enums.EnvironmentType;
import com.campestre.enums.RoleType;
import com.campestre.repositories.EnvironmentRepository;
import com.campestre.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   EnvironmentRepository environmentRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Inicializando datos por defecto...");

                userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .nombreCompleto("Administrador")
                        .email("admin@campestre.com")
                        .role(RoleType.ADMIN)
                        .activo(true)
                        .build());

                userRepository.save(User.builder()
                        .username("asistente")
                        .password(passwordEncoder.encode("asistente123"))
                        .nombreCompleto("Asistente Principal")
                        .email("asistente@campestre.com")
                        .role(RoleType.ASISTENTE)
                        .activo(true)
                        .build());

                log.info("Usuarios creados: admin/admin123, asistente/asistente123");
            }

            if (environmentRepository.count() == 0) {
                environmentRepository.save(Environment.builder()
                        .nombre("Piscina + Salón")
                        .tipo(EnvironmentType.EVENTO)
                        .descripcion("Amplia piscina con salón de eventos techado, ideal para reuniones familiares y empresariales")
                        .precioBase(java.math.BigDecimal.valueOf(1500))
                        .capacidadMaxima(100)
                        .estado(EnvironmentStatus.ACTIVO)
                        .build());

                environmentRepository.save(Environment.builder()
                        .nombre("Salón Principal")
                        .tipo(EnvironmentType.EVENTO)
                        .descripcion("Salón principal con capacidad para grandes eventos, completamente equipado")
                        .precioBase(java.math.BigDecimal.valueOf(2000))
                        .capacidadMaxima(200)
                        .estado(EnvironmentStatus.ACTIVO)
                        .build());

                environmentRepository.save(Environment.builder()
                        .nombre("Áreas Verdes")
                        .tipo(EnvironmentType.EVENTO)
                        .descripcion("Extensas áreas verdes con jardines, ideal para eventos al aire libre")
                        .precioBase(java.math.BigDecimal.valueOf(1200))
                        .capacidadMaxima(150)
                        .estado(EnvironmentStatus.ACTIVO)
                        .build());

                environmentRepository.save(Environment.builder()
                        .nombre("Cancha de Grass")
                        .tipo(EnvironmentType.HORAS)
                        .descripcion("Cancha de grass sintético para fútbol y deportes, alquiler por horas")
                        .precioBase(java.math.BigDecimal.valueOf(80))
                        .capacidadMaxima(22)
                        .estado(EnvironmentStatus.ACTIVO)
                        .build());

                log.info("Ambientes creados: Piscina+Salón, Salón Principal, Áreas Verdes, Cancha de Grass");
            }
        };
    }
}
