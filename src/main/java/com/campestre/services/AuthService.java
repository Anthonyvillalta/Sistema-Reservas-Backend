package com.campestre.services;

import com.campestre.dto.request.LoginRequest;
import com.campestre.dto.request.RegisterRequest;
import com.campestre.dto.response.AuthResponse;
import com.campestre.dto.response.UserResponse;
import com.campestre.entities.User;
import com.campestre.enums.RoleType;
import com.campestre.exceptions.BusinessException;
import com.campestre.exceptions.ResourceNotFoundException;
import com.campestre.repositories.UserRepository;
import com.campestre.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.getUsername()));

        String token = jwtTokenProvider.generateToken(
                user.getUsername(), user.getRole().name(), user.getId(), user.getNombreCompleto());

        log.info("Login exitoso: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .nombreCompleto(user.getNombreCompleto())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El username '" + request.getUsername() + "' ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email '" + request.getEmail() + "' ya está registrado");
        }

        RoleType role = request.getRole() != null ? request.getRole() : RoleType.ASISTENTE;

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .role(role)
                .activo(true)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                user.getUsername(), user.getRole().name(), user.getId(), user.getNombreCompleto());

        log.info("Usuario registrado: {} con rol {}", user.getUsername(), role);

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .nombreCompleto(user.getNombreCompleto())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nombreCompleto(user.getNombreCompleto())
                .email(user.getEmail())
                .role(user.getRole())
                .activo(user.getActivo())
                .build();
    }
}
