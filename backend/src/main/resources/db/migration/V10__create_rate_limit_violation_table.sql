CREATE TABLE IF NOT EXISTS rate_limit_violation (
                                                    bucket_key BIGINT PRIMARY KEY,
                                                    violation_count INTEGER NOT NULL,
                                                    penalty_until TIMESTAMP NULL,
                                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);