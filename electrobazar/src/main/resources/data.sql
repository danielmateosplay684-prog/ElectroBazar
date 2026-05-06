-- =============================================================================
-- ELECTOBAZAR INITIAL SEED DATA
-- =============================================================================

-- 1. SEED TAX RATES (Spanish standard and reduced rates)
INSERT IGNORE INTO tax_rates (id, vat_rate, re_rate, description, active, valid_from) 
VALUES (1, 0.2100, 0.0520, 'IVA General', 1, '2024-01-01');

INSERT IGNORE INTO tax_rates (id, vat_rate, re_rate, description, active, valid_from) 
VALUES (2, 0.1000, 0.0140, 'IVA Reducido', 1, '2024-01-01');

INSERT IGNORE INTO tax_rates (id, vat_rate, re_rate, description, active, valid_from) 
VALUES (3, 0.0400, 0.0050, 'IVA Superreducido', 1, '2024-01-01');

-- 2. ROLES PROVISIONING
INSERT IGNORE INTO roles (id, name, description) VALUES (1, 'ADMIN', 'Administrador del sistema');
INSERT IGNORE INTO roles (id, name, description) VALUES (2, 'ENCARGADO', 'Encargado de tienda');
INSERT IGNORE INTO roles (id, name, description) VALUES (3, 'VENDEDOR', 'Vendedor de tienda');

-- 3. PERMISSIONS SYNC (Matching SecurityConfig.java)
DELETE FROM role_permissions;

-- ADMIN: ACCESO_TOTAL_ADMIN
INSERT INTO role_permissions (role_id, permission) VALUES (1, 'ACCESO_TOTAL_ADMIN');

-- ENCARGADO: TPV access, closing, returns, hold sales, CRM
INSERT INTO role_permissions (role_id, permission) VALUES (2, 'ACCESO_TPV');
INSERT INTO role_permissions (role_id, permission) VALUES (2, 'CIERRE_CAJA');
INSERT INTO role_permissions (role_id, permission) VALUES (2, 'GESTION_DEVOLUCIONES');
INSERT INTO role_permissions (role_id, permission) VALUES (2, 'GESTION_VENTAS_PAUSADAS');
INSERT INTO role_permissions (role_id, permission) VALUES (2, 'GESTION_CLIENTES_CRM');
INSERT INTO role_permissions (role_id, permission) VALUES (2, 'VER_VENTAS');

-- VENDEDOR: TPV access, closing, hold sales
INSERT INTO role_permissions (role_id, permission) VALUES (3, 'ACCESO_TPV');
INSERT INTO role_permissions (role_id, permission) VALUES (3, 'CIERRE_CAJA');
INSERT INTO role_permissions (role_id, permission) VALUES (3, 'GESTION_VENTAS_PAUSADAS');

-- 4. COMPANY SETTINGS (ID 1)
INSERT IGNORE INTO company_settings (id, app_name, name, cif, address, city, postal_code, phone, email, website, return_deadline_days)
VALUES (1, 'Electrobazar', 'Electrobazar S.L.', 'B12345678', 'Calle Principal 123', 'León', '24001', '987654321', 'contacto@electrobazar.com', 'www.electrobazar.com', 15);

-- 5. DEMO CATEGORIES
INSERT IGNORE INTO categories (id, name_es, description_es, active) VALUES (1, 'Electrónica', 'Dispositivos y gadgets', 1);
INSERT IGNORE INTO categories (id, name_es, description_es, active) VALUES (2, 'Electrodomésticos', 'Para el hogar', 1);

-- 6. DEMO PRODUCTS
-- Note: tax_rate_id 1 is 21% General
INSERT IGNORE INTO products (id, name_es, price, base_price_net, stock, active, category_id, tax_rate_id, sales_rank)
VALUES (1, 'Smartphone Pro X', 599.00, 495.0413, 10, 1, 1, 1, 5);

INSERT IGNORE INTO products (id, name_es, price, base_price_net, stock, active, category_id, tax_rate_id, sales_rank)
VALUES (2, 'Auriculares Wireless', 89.90, 74.2975, 25, 1, 1, 1, 10);

INSERT IGNORE INTO products (id, name_es, price, base_price_net, stock, active, category_id, tax_rate_id, sales_rank)
VALUES (3, 'Cafetera Express', 129.00, 106.6116, 5, 1, 2, 1, 3);

-- 7. CLEANUP IMAGE URLs (Legacy patches)
UPDATE products SET image_url = REPLACE(image_url, '/uploads/products/', '/img/')
WHERE image_url LIKE '/uploads/products/%';
