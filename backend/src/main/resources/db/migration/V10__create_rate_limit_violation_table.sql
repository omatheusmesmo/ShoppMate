CREATE TABLE IF NOT EXISTS rate_limit_violation (
                                                    bucket_key BIGINT PRIMARY KEY,
                                                    violation_count INTEGER NOT NULL,
                                                    penalty_until TIMESTAMPTZ NULL,
                                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
