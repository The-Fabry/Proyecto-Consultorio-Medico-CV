
-- ============================================
-- MODULO DE HISTORIAL CLINICO
-- Responsable: Nando
-- Proyecto: Consultorio Medico CV
-- ============================================

USE consultorio_db;

-- Crear tabla para registrar diagnosticos
-- y notas medicas de cada cita.

CREATE TABLE IF NOT EXISTS historial_clinico (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,

    id_cita INT NOT NULL UNIQUE,

    diagnostico TEXT NOT NULL,

    notas TEXT,

    fecha_registro TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_cita
        FOREIGN KEY (id_cita)
        REFERENCES citas(id_cita)
        ON DELETE RESTRICT
        ON UPDATE CASCADE

) ENGINE=InnoDB;
