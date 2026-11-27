-- V5: Marketplace, suppliers, carts, orders, buyers, produce listings, and market prices

CREATE TABLE IF NOT EXISTS product_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(96) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(48) NOT NULL DEFAULT 'Sprout',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS suppliers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    business_name VARCHAR(160) NOT NULL,
    gstin VARCHAR(32),
    description TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    phone VARCHAR(32),
    verification_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers (id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES product_categories (id) ON DELETE RESTRICT,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    unit VARCHAR(32) NOT NULL,
    price_inr NUMERIC(14, 2) NOT NULL CHECK (price_inr > 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    brand VARCHAR(100),
    image_url VARCHAR(500),
    approval_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    moderation_note VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_products_category_status ON products (category_id, approval_status, is_active);
CREATE INDEX IF NOT EXISTS ix_products_supplier ON products (supplier_id);

CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT_CONFIG',
    payment_status VARCHAR(32) NOT NULL DEFAULT 'UNCONFIGURED',
    payment_provider VARCHAR(48) NOT NULL DEFAULT 'unconfigured',
    total_amount_inr NUMERIC(14, 2) NOT NULL CHECK (total_amount_inr >= 0),
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    shipping_name VARCHAR(120) NOT NULL,
    shipping_phone VARCHAR(32) NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_orders_user ON orders (user_id, created_at);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID REFERENCES products (id) ON DELETE SET NULL,
    supplier_id UUID NOT NULL REFERENCES suppliers (id) ON DELETE RESTRICT,
    product_name_snapshot VARCHAR(180) NOT NULL,
    unit_snapshot VARCHAR(32) NOT NULL,
    unit_price_inr NUMERIC(14, 2) NOT NULL CHECK (unit_price_inr > 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    line_total_inr NUMERIC(14, 2) NOT NULL,
    fulfillment_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_order_items_order ON order_items (order_id);
CREATE INDEX IF NOT EXISTS ix_order_items_supplier ON order_items (supplier_id, fulfillment_status);

CREATE TABLE IF NOT EXISTS buyers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    organization_name VARCHAR(160),
    buyer_type VARCHAR(48) NOT NULL DEFAULT 'RETAIL',
    city VARCHAR(100),
    state VARCHAR(100),
    preferred_crops TEXT,
    verification_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS produce_listings (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    crop_name VARCHAR(120) NOT NULL,
    variety VARCHAR(120),
    quantity DOUBLE PRECISION NOT NULL CHECK (quantity > 0),
    unit VARCHAR(32) NOT NULL DEFAULT 'QUINTAL',
    expected_price_inr NUMERIC(14, 2) NOT NULL CHECK (expected_price_inr > 0),
    location VARCHAR(180) NOT NULL,
    quality_grade VARCHAR(64) NOT NULL,
    quality_notes TEXT,
    available_from DATE,
    harvest_date DATE,
    listing_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    moderation_status VARCHAR(24) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_produce_listings_farmer ON produce_listings (farmer_id);
CREATE INDEX IF NOT EXISTS ix_produce_listings_status ON produce_listings (listing_status, moderation_status);

CREATE TABLE IF NOT EXISTS produce_inquiries (
    id UUID PRIMARY KEY,
    produce_listing_id UUID NOT NULL REFERENCES produce_listings (id) ON DELETE CASCADE,
    buyer_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    offered_price_inr NUMERIC(14, 2),
    requested_quantity DOUBLE PRECISION NOT NULL CHECK (requested_quantity > 0),
    message TEXT NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
    farmer_reply TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_produce_inquiries_listing ON produce_inquiries (produce_listing_id);

CREATE TABLE IF NOT EXISTS market_prices (
    id UUID PRIMARY KEY,
    commodity VARCHAR(120) NOT NULL,
    variety VARCHAR(120),
    market_name VARCHAR(150) NOT NULL,
    district VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    min_price_inr NUMERIC(14, 2) NOT NULL,
    max_price_inr NUMERIC(14, 2) NOT NULL,
    modal_price_inr NUMERIC(14, 2) NOT NULL,
    unit VARCHAR(32) NOT NULL DEFAULT 'QUINTAL',
    price_date DATE NOT NULL,
    data_source VARCHAR(64) NOT NULL,
    is_sample_data BOOLEAN NOT NULL DEFAULT TRUE,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_market_prices_commodity_date ON market_prices (commodity, price_date);
CREATE INDEX IF NOT EXISTS ix_market_prices_state ON market_prices (state);
