CREATE TABLE IF NOT EXISTS rate_limit_violation (
                                                    id BIGINT PRIMARY KEY,
                                                    bucket_key BIGINT NOT NULL,
                                                    violation_count INTEGER NOT NULL,
                                                    penalty_until TIMESTAMPTZ NULL,
                                                    version BIGINT NOT NULL DEFAULT 0,
                                                    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
    );

CREATE UNIQUE INDEX idx_rate_limit_violation_bucket_key
    ON rate_limit_violation (bucket_key)
    WHERE deleted = FALSE;

CREATE INDEX idx_rate_limit_violation_deleted
    ON rate_limit_violation (deleted);
