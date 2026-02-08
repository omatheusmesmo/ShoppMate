CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_deleted ON users (deleted);