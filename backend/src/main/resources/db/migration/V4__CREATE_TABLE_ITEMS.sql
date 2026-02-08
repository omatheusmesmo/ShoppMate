CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    id_category INTEGER,
    id_unit INTEGER,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE (name),
    FOREIGN KEY (id_category) REFERENCES categories(id),
    FOREIGN KEY (id_unit) REFERENCES units(id)
);

CREATE INDEX idx_items_id_category ON items (id_category);
CREATE INDEX idx_items_id_unit ON items (id_unit);
CREATE INDEX idx_items_name ON items (name);
CREATE INDEX idx_items_deleted ON items (deleted);