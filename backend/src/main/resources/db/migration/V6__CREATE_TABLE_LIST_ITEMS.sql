CREATE TABLE list_items (
    id SERIAL PRIMARY KEY,
    id_list INTEGER NOT NULL,
    id_item INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    purchased BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE (id_list, id_item),
    FOREIGN KEY (id_list) REFERENCES lists(id),
    FOREIGN KEY (id_item) REFERENCES items(id)
);

CREATE INDEX idx_list_items_id_list ON list_items (id_list);
CREATE INDEX idx_list_items_id_item ON list_items (id_item);
CREATE INDEX idx_list_items_deleted ON list_items (deleted);