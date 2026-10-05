-- ==========================================================
-- Sistema de Facturación — esquema de base de datos
-- Servidor: PostgreSQL 18 (localhost:5433)
--
-- 1) Crear la base (conectado a la base "postgres"):
--      CREATE DATABASE facturacion_db;
-- 2) Conectarse a facturacion_db y ejecutar el resto del script.
-- ==========================================================

CREATE TABLE IF NOT EXISTS categorias (
    id     SERIAL       PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    activa BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS productos (
    id           SERIAL        PRIMARY KEY,
    codigo       VARCHAR(30)   NOT NULL UNIQUE,
    nombre       VARCHAR(150)  NOT NULL,
    -- Al eliminar una categoría, sus productos quedan sin categoría (NULL)
    categoria_id INTEGER       REFERENCES categorias(id) ON DELETE SET NULL,
    precio_venta NUMERIC(12,2) NOT NULL CHECK (precio_venta > 0),
    existencia   INTEGER       NOT NULL CHECK (existencia >= 0),
    activo       BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Datos de ejemplo (solo si la tabla está vacía)
INSERT INTO categorias (nombre, activa)
SELECT v.nombre, v.activa
FROM (VALUES
          ('Electrónica', TRUE),
          ('Papelería',   TRUE),
          ('Alimentos',   TRUE)
     ) AS v(nombre, activa)
WHERE NOT EXISTS (SELECT 1 FROM categorias);
