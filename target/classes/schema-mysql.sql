-- ============================================
-- Centro Recreacional Campestre José Antonio
-- Schema de Base de Datos MySQL
-- ============================================

CREATE DATABASE IF NOT EXISTS campestre_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campestre_db;

-- ============================================
-- USERS
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'ASISTENTE',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- ============================================
-- CLIENTS
-- ============================================
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    celular VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    documento_identidad VARCHAR(20) UNIQUE,
    direccion VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_nombre (nombre),
    INDEX idx_celular (celular)
) ENGINE=InnoDB;

-- ============================================
-- ENVIRONMENTS (Ambientes)
-- ============================================
CREATE TABLE IF NOT EXISTS environments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL,
    descripcion TEXT,
    precio_base DECIMAL(10,2) NOT NULL,
    capacidad_maxima INT,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    imagen_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tipo (tipo),
    INDEX idx_estado (estado)
) ENGINE=InnoDB;

-- ============================================
-- RESERVATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_reserva VARCHAR(20) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    environment_id BIGINT NOT NULL,
    fecha_evento DATE NOT NULL,
    hora_inicio TIME,
    hora_fin TIME,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'RESERVADO',
    precio_total DECIMAL(10,2) NOT NULL,
    adelanto_requerido BOOLEAN DEFAULT FALSE,
    notas TEXT,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (environment_id) REFERENCES environments(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_codigo (codigo_reserva),
    INDEX idx_environment_fechas (environment_id, fecha_inicio, fecha_fin),
    INDEX idx_client_fecha (client_id, fecha_inicio),
    INDEX idx_estado (estado),
    INDEX idx_fecha_evento (fecha_evento)
) ENGINE=InnoDB;

-- ============================================
-- PAYMENTS
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    tipo_pago VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATETIME NOT NULL,
    referencia VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    INDEX idx_reservation (reservation_id),
    INDEX idx_fecha_pago (fecha_pago)
) ENGINE=InnoDB;

-- ============================================
-- MAINTENANCES
-- ============================================
CREATE TABLE IF NOT EXISTS maintenances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'MANTENIMIENTO',
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (environment_id) REFERENCES environments(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_environment (environment_id),
    INDEX idx_fechas (fecha_inicio, fecha_fin)
) ENGINE=InnoDB;

-- ============================================
-- NOTIFICATIONS
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    destinatario VARCHAR(100) NOT NULL,
    asunto VARCHAR(200),
    mensaje TEXT NOT NULL,
    estado_envio VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    INDEX idx_reservation (reservation_id),
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB;
