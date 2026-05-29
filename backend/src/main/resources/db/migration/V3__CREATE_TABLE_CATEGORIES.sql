CREATE TABLE categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_system_standard BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id BIGINT,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_categories_name_system ON categories (name) WHERE is_system_standard = TRUE;
CREATE UNIQUE INDEX idx_categories_owner_name ON categories (owner_id, name) WHERE owner_id IS NOT NULL;
CREATE INDEX idx_categories_deleted ON categories (deleted);

ALTER TABLE categories
ADD CONSTRAINT fk_categories_owner_id_users_id
FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_categories_is_system_standard ON categories (is_system_standard);
CREATE INDEX idx_categories_owner_id ON categories (owner_id);