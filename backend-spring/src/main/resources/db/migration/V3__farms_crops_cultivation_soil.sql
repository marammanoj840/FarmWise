-- V3: Farms, locations, crops, varieties, cultivation, soil records, and crop intelligence

CREATE TABLE IF NOT EXISTS farms (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    land_area DOUBLE PRECISION NOT NULL CHECK (land_area > 0),
    area_unit VARCHAR(24) NOT NULL DEFAULT 'ACRES',
    soil_type VARCHAR(64) NOT NULL,
    irrigation_type VARCHAR(64) NOT NULL,
    photo_urls_json TEXT NOT NULL DEFAULT '[]',
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_farms_farmer_id ON farms (farmer_id);

CREATE TABLE IF NOT EXISTS farm_locations (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL UNIQUE REFERENCES farms (id) ON DELETE CASCADE,
    address_line VARCHAR(255),
    village_or_city VARCHAR(120) NOT NULL,
    district VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    climate_zone VARCHAR(80),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS crops (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    scientific_name VARCHAR(150),
    category VARCHAR(64) NOT NULL,
    typical_duration_days INTEGER,
    water_requirement VARCHAR(64),
    optimal_ph_min DOUBLE PRECISION,
    optimal_ph_max DOUBLE PRECISION,
    season_suitability VARCHAR(120),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS crop_varieties (
    id UUID PRIMARY KEY,
    crop_id UUID NOT NULL REFERENCES crops (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    maturity_days INTEGER,
    expected_yield_per_acre_kg DOUBLE PRECISION,
    climate_notes TEXT,
    disease_resistance TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_crop_variety_name UNIQUE (crop_id, name)
);

CREATE INDEX IF NOT EXISTS ix_crop_varieties_crop_id ON crop_varieties (crop_id);

CREATE TABLE IF NOT EXISTS farm_crops (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms (id) ON DELETE CASCADE,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    crop_id UUID REFERENCES crops (id) ON DELETE SET NULL,
    variety_id UUID REFERENCES crop_varieties (id) ON DELETE SET NULL,
    crop_name VARCHAR(120) NOT NULL,
    variety_name VARCHAR(120),
    season VARCHAR(40) NOT NULL,
    planted_area DOUBLE PRECISION NOT NULL CHECK (planted_area > 0),
    area_unit VARCHAR(24) NOT NULL DEFAULT 'ACRES',
    sowing_date DATE,
    expected_harvest_date DATE,
    actual_harvest_date DATE,
    growth_stage VARCHAR(40) NOT NULL DEFAULT 'PLANNING',
    cultivation_status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    expected_yield_kg DOUBLE PRECISION,
    actual_yield_kg DOUBLE PRECISION,
    field_observations TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_farm_crops_farm_id ON farm_crops (farm_id);
CREATE INDEX IF NOT EXISTS ix_farm_crops_farmer_id ON farm_crops (farmer_id);
CREATE INDEX IF NOT EXISTS ix_farm_crops_status ON farm_crops (cultivation_status);

CREATE TABLE IF NOT EXISTS cultivation_activities (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms (id) ON DELETE CASCADE,
    farm_crop_id UUID REFERENCES farm_crops (id) ON DELETE SET NULL,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    stage VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    activity_date DATE NOT NULL,
    input_used VARCHAR(160),
    quantity DOUBLE PRECISION,
    unit VARCHAR(32),
    cost_amount NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (cost_amount >= 0),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_cultivation_activities_farmer ON cultivation_activities (farmer_id, activity_date);
CREATE INDEX IF NOT EXISTS ix_cultivation_activities_farm_crop ON cultivation_activities (farm_crop_id);

CREATE TABLE IF NOT EXISTS cultivation_tasks (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms (id) ON DELETE CASCADE,
    farm_crop_id UUID REFERENCES farm_crops (id) ON DELETE SET NULL,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    stage VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    due_date DATE NOT NULL,
    reminder_at TIMESTAMP WITH TIME ZONE,
    priority VARCHAR(24) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_cultivation_tasks_farmer_due ON cultivation_tasks (farmer_id, status, due_date);

CREATE TABLE IF NOT EXISTS soil_records (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms (id) ON DELETE CASCADE,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    soil_type VARCHAR(64) NOT NULL,
    ph DOUBLE PRECISION NOT NULL CHECK (ph >= 0 AND ph <= 14),
    nitrogen DOUBLE PRECISION CHECK (nitrogen >= 0),
    phosphorus DOUBLE PRECISION CHECK (phosphorus >= 0),
    potassium DOUBLE PRECISION CHECK (potassium >= 0),
    organic_carbon_pct DOUBLE PRECISION,
    moisture_pct DOUBLE PRECISION,
    fertility_rating VARCHAR(40),
    lab_name VARCHAR(140),
    test_date DATE NOT NULL,
    observations TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_soil_records_farm ON soil_records (farm_id, test_date);
CREATE INDEX IF NOT EXISTS ix_soil_records_farmer ON soil_records (farmer_id);

CREATE TABLE IF NOT EXISTS crop_observations (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms (id) ON DELETE CASCADE,
    farm_crop_id UUID REFERENCES farm_crops (id) ON DELETE SET NULL,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    observation_type VARCHAR(40) NOT NULL,
    severity VARCHAR(24) NOT NULL DEFAULT 'LOW',
    title VARCHAR(160) NOT NULL,
    symptoms TEXT NOT NULL,
    affected_area_pct DOUBLE PRECISION,
    recommended_action TEXT,
    expert_verified BOOLEAN NOT NULL DEFAULT FALSE,
    observed_at DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_crop_observations_farmer ON crop_observations (farmer_id, observed_at);

CREATE TABLE IF NOT EXISTS crop_knowledge_base (
    id UUID PRIMARY KEY,
    crop_name VARCHAR(100) NOT NULL,
    category VARCHAR(64) NOT NULL,
    suitable_soil_types VARCHAR(255) NOT NULL,
    ideal_ph_range VARCHAR(64) NOT NULL,
    sowing_season VARCHAR(120) NOT NULL,
    irrigation_schedule TEXT NOT NULL,
    nutrient_guidance TEXT NOT NULL,
    common_pests_diseases TEXT NOT NULL,
    advisory_disclaimer TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_crop_knowledge_name ON crop_knowledge_base (crop_name);
