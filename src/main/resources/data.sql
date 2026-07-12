-- ============================================
-- Centro Recreacional Campestre José Antonio
-- Datos Iniciales (Seed)
-- ============================================
-- Contraseña para ambos: admin123 / asistente123
-- (BCrypt hash generado)

INSERT INTO users (username, password, nombre_completo, email, role, activo) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'admin@campestre.com', 'ADMIN', TRUE),
('asistente', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Asistente Principal', 'asistente@campestre.com', 'ASISTENTE', TRUE);

INSERT INTO environments (nombre, tipo, descripcion, precio_base, capacidad_maxima, estado) VALUES
('Piscina + Salón', 'EVENTO', 'Amplia piscina con salón de eventos techado, ideal para reuniones familiares y empresariales', 1500.00, 100, 'ACTIVO'),
('Salón Principal', 'EVENTO', 'Salón principal con capacidad para grandes eventos, completamente equipado', 2000.00, 200, 'ACTIVO'),
('Áreas Verdes', 'EVENTO', 'Extensas áreas verdes con jardines, ideal para eventos al aire libre', 1200.00, 150, 'ACTIVO'),
('Cancha de Grass', 'HORAS', 'Cancha de grass sintético para fútbol y deportes, alquiler por horas', 80.00, 22, 'ACTIVO');
