CREATE TABLE units (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    symbol VARCHAR(20),
    is_system_standard BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id BIGINT,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_units_name ON units (name);
CREATE INDEX idx_units_deleted ON units (deleted);

ALTER TABLE units
ADD CONSTRAINT fk_units_owner_id_users_id
FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_units_is_system_standard ON units (is_system_standard);
CREATE INDEX idx_units_owner_id ON units (owner_id);