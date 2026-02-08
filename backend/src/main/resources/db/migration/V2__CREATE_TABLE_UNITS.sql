CREATE TABLE units (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    symbol VARCHAR(20),
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_units_name ON units (name);
CREATE INDEX idx_units_deleted ON units (deleted);