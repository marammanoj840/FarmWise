-- FarmWise Backend Foundation: initial schema verification table

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO roles (id, name, description, created_at, updated_at)
SELECT '11111111-1111-1111-1111-111111111101', 'ADMIN', 'Platform administrator with governance and moderation permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (id, name, description, created_at, updated_at)
SELECT '11111111-1111-1111-1111-111111111102', 'FARMER', 'Agricultural producer managing farms, crops, inventory, and marketplace listings', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'FARMER');

INSERT INTO roles (id, name, description, created_at, updated_at)
SELECT '11111111-1111-1111-1111-111111111103', 'CUSTOMER', 'Marketplace buyer browsing, ordering, and reviewing agricultural products', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CUSTOMER');
