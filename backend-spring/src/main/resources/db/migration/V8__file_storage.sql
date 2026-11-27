-- V8: File storage objects table for media and documents
CREATE TABLE IF NOT EXISTS file_objects (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    original_filename VARCHAR(500) NOT NULL,
    storage_key VARCHAR(1000) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'local-metadata',
    public_url VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_file_objects_owner ON file_objects (owner_id);
CREATE INDEX IF NOT EXISTS ix_file_objects_entity ON file_objects (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS ix_file_objects_storage_key ON file_objects (storage_key);
