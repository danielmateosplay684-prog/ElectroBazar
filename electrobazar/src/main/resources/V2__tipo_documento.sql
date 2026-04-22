-- Migración: sistema de tipos de documento (FACTURA_COMPLETA / FACTURA_SIMPLIFICADA)
-- Ejecutar sobre la BD antes de arrancar la app con estos cambios.

-- 1. Añadir tipo_documento (nullable para compatibilidad con registros existentes)
ALTER TABLE sales
    ADD COLUMN tipo_documento ENUM('FACTURA_COMPLETA', 'FACTURA_SIMPLIFICADA') NULL
        AFTER abono_amount;

-- 2. Añadir cliente_puntual_json (datos de factura puntual sin guardar cliente en BD)
ALTER TABLE sales
    ADD COLUMN cliente_puntual_json TEXT NULL
        AFTER tipo_documento;

-- 3. Retroalimentar registros existentes:
--    Ventas con factura asociada → FACTURA_COMPLETA
--    Resto → FACTURA_SIMPLIFICADA
UPDATE sales s
    INNER JOIN invoices i ON i.sale_id = s.id
SET s.tipo_documento = 'FACTURA_COMPLETA'
WHERE s.tipo_documento IS NULL;

UPDATE sales
SET tipo_documento = 'FACTURA_SIMPLIFICADA'
WHERE tipo_documento IS NULL;

-- 4. Marcar NOT NULL ahora que todos los registros tienen valor
ALTER TABLE sales
    MODIFY COLUMN tipo_documento ENUM('FACTURA_COMPLETA', 'FACTURA_SIMPLIFICADA') NOT NULL;
