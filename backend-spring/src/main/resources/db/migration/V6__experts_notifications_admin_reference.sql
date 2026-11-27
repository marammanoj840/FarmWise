-- V6: Experts, consultations, appointments, reviews, notifications, audit logs, and admin

CREATE TABLE IF NOT EXISTS experts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    headline VARCHAR(180) NOT NULL,
    specializations VARCHAR(255) NOT NULL,
    qualifications VARCHAR(255),
    experience_years INTEGER NOT NULL DEFAULT 0,
    languages VARCHAR(160) NOT NULL DEFAULT 'English, Hindi',
    city VARCHAR(100),
    state VARCHAR(100),
    consultation_fee_inr NUMERIC(12, 2) NOT NULL DEFAULT 0,
    bio TEXT,
    verification_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    verified_by UUID REFERENCES users (id) ON DELETE SET NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_experts_verification ON experts (verification_status, is_available);

CREATE TABLE IF NOT EXISTS consultation_requests (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expert_id UUID NOT NULL REFERENCES experts (id) ON DELETE CASCADE,
    farm_id UUID REFERENCES farms (id) ON DELETE SET NULL,
    topic VARCHAR(180) NOT NULL,
    crop_name VARCHAR(120),
    question TEXT NOT NULL,
    urgency VARCHAR(24) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    expert_response TEXT,
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_consultation_farmer ON consultation_requests (farmer_id, status);
CREATE INDEX IF NOT EXISTS ix_consultation_expert ON consultation_requests (expert_id, status);

CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY,
    consultation_request_id UUID REFERENCES consultation_requests (id) ON DELETE SET NULL,
    farmer_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expert_id UUID NOT NULL REFERENCES experts (id) ON DELETE CASCADE,
    scheduled_start TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end TIMESTAMP WITH TIME ZONE NOT NULL,
    meeting_mode VARCHAR(32) NOT NULL DEFAULT 'VIDEO',
    meeting_notes TEXT,
    status VARCHAR(24) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_appointments_expert_start ON appointments (expert_id, scheduled_start);
CREATE INDEX IF NOT EXISTS ix_appointments_farmer_start ON appointments (farmer_id, scheduled_start);

CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY,
    reviewer_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type VARCHAR(32) NOT NULL,
    target_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(140) NOT NULL,
    comment TEXT NOT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_reviews_target ON reviews (target_type, target_id, is_visible);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    action_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_notifications_user_read ON notifications (user_id, is_read, created_at);

CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    cultivation_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    weather_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    inventory_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    order_updates BOOLEAN NOT NULL DEFAULT TRUE,
    expert_updates BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    actor_email VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    details_json TEXT NOT NULL DEFAULT '{}',
    ip_address VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_audit_logs_actor ON audit_logs (actor_user_id, created_at);
CREATE INDEX IF NOT EXISTS ix_audit_logs_action ON audit_logs (action, created_at);

CREATE TABLE IF NOT EXISTS contact_inquiries (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    role_interest VARCHAR(64),
    subject VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
    admin_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_contact_inquiries_status ON contact_inquiries (status, created_at);

CREATE TABLE IF NOT EXISTS system_settings (
    id UUID PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
