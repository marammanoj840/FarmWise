-- V4: Inventory, transactions, farm finance (expenses/income), weather, and AI assistant conversations

CREATE TABLE IF NOT EXISTS inventory_items (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(40) NOT NULL,
    sku VARCHAR(64),
    quantity DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    unit VARCHAR(32) NOT NULL,
    reorder_threshold DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (reorder_threshold >= 0),
    unit_cost_inr NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (unit_cost_inr >= 0),
    supplier_name VARCHAR(150),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_inventory_items_farmer ON inventory_items (farmer_id, category);

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id UUID PRIMARY KEY,
    inventory_item_id UUID NOT NULL REFERENCES inventory_items (id) ON DELETE CASCADE,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    transaction_type VARCHAR(24) NOT NULL,
    quantity_delta DOUBLE PRECISION NOT NULL,
    unit_cost_inr NUMERIC(14, 2) NOT NULL DEFAULT 0,
    reference_note VARCHAR(255),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_inventory_tx_item ON inventory_transactions (inventory_item_id, transaction_date);

CREATE TABLE IF NOT EXISTS expenses (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    farm_crop_id UUID REFERENCES farm_crops (id) ON DELETE SET NULL,
    category VARCHAR(48) NOT NULL,
    title VARCHAR(160) NOT NULL,
    amount_inr NUMERIC(14, 2) NOT NULL CHECK (amount_inr > 0),
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    expense_date DATE NOT NULL,
    vendor_name VARCHAR(140),
    payment_method VARCHAR(48),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_expenses_farmer_date ON expenses (farmer_id, expense_date);
CREATE INDEX IF NOT EXISTS ix_expenses_farm ON expenses (farm_id);

CREATE TABLE IF NOT EXISTS income (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    farm_crop_id UUID REFERENCES farm_crops (id) ON DELETE SET NULL,
    category VARCHAR(48) NOT NULL,
    title VARCHAR(160) NOT NULL,
    amount_inr NUMERIC(14, 2) NOT NULL CHECK (amount_inr > 0),
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    income_date DATE NOT NULL,
    buyer_name VARCHAR(140),
    quantity_sold DOUBLE PRECISION,
    unit VARCHAR(32),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_income_farmer_date ON income (farmer_id, income_date);
CREATE INDEX IF NOT EXISTS ix_income_farm ON income (farm_id);

CREATE TABLE IF NOT EXISTS weather_records (
    id UUID PRIMARY KEY,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    location_name VARCHAR(150) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    temperature_c DOUBLE PRECISION,
    humidity_pct DOUBLE PRECISION,
    rainfall_mm DOUBLE PRECISION,
    wind_speed_kmh DOUBLE PRECISION,
    condition_summary VARCHAR(140),
    alert_level VARCHAR(24) NOT NULL DEFAULT 'NONE',
    agronomic_advice TEXT,
    provider_name VARCHAR(64) NOT NULL,
    is_live_data BOOLEAN NOT NULL DEFAULT FALSE,
    observed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_weather_records_farm_obs ON weather_records (farm_id, observed_at);

CREATE TABLE IF NOT EXISTS assistant_conversations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    title VARCHAR(180) NOT NULL,
    provider_name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_assistant_conv_user ON assistant_conversations (user_id, updated_at);

CREATE TABLE IF NOT EXISTS assistant_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES assistant_conversations (id) ON DELETE CASCADE,
    sender_role VARCHAR(24) NOT NULL,
    content TEXT NOT NULL,
    is_ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    context_summary VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_assistant_msg_conv ON assistant_messages (conversation_id, created_at);
