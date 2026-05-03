ALTER TABLE categories
ADD COLUMN is_system_standard BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN owner_id BIGINT;

ALTER TABLE units
ADD COLUMN is_system_standard BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN owner_id BIGINT;

UPDATE categories SET is_system_standard = TRUE WHERE is_system_standard = FALSE;
UPDATE units SET is_system_standard = TRUE WHERE is_system_standard = FALSE;

ALTER TABLE categories
ADD CONSTRAINT fk_categories_owner_id_users_id
FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE units
ADD CONSTRAINT fk_units_owner_id_users_id
FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_categories_is_system_standard ON categories (is_system_standard);
CREATE INDEX idx_categories_owner_id ON categories (owner_id);
CREATE INDEX idx_units_is_system_standard ON units (is_system_standard);
CREATE INDEX idx_units_owner_id ON units (owner_id);
