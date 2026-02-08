CREATE TABLE lists (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id_user INTEGER NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (owner_id_user) REFERENCES users(id)
);

CREATE INDEX idx_lists_owner_id_user ON lists (owner_id_user);
CREATE INDEX idx_lists_deleted ON lists (deleted);