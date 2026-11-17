-- Radio Jugoseo
-- Base de datos exclusiva del microservicio de usuarios

CREATE DATABASE IF NOT EXISTS radiojugoseo_usuarios
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE radiojugoseo_usuarios;

-- Roles del sistema
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

-- Cuentas de usuario
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    run VARCHAR(9) UNIQUE,
    fecha_nacimiento DATE,
    direccion VARCHAR(300),
    region VARCHAR(100),
    comuna VARCHAR(100),
    tipo VARCHAR(30),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version_cuenta BIGINT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    rol_id BIGINT NOT NULL,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles(id)
);

-- Roles iniciales
INSERT IGNORE INTO roles (nombre)
VALUES
    ('Cliente'),
    ('Locutor'),
    ('Administrador');
