package com.omatheusmesmo.shoppmate.auth.configs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class RateLimitViolationTracker {

    private static final int MAX_EXPONENT = 5;

    private final JdbcTemplate jdbcTemplate;
    private final RateLimitProperties properties;

    public RateLimitViolationTracker(JdbcTemplate jdbcTemplate, RateLimitProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public Optional<Long> getActivePenaltySeconds(Long bucketKey) {
        return jdbcTemplate.query("""
                SELECT penalty_until
                FROM rate_limit_violation
                WHERE bucket_key = ?
                """, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }

            Timestamp penaltyUntilTimestamp = rs.getTimestamp("penalty_until");

            if (penaltyUntilTimestamp == null) {
                return Optional.empty();
            }

            Instant penaltyUntil = penaltyUntilTimestamp.toInstant();
            Instant now = Instant.now();

            if (penaltyUntil.isAfter(now)) {
                long seconds = Duration.between(now, penaltyUntil).toSeconds();
                return Optional.of(Math.max(1, seconds));
            }

            return Optional.empty();
        }, bucketKey);
    }

    public long recordViolationAndCalculatePenaltySeconds(Long bucketKey, long baseSecondsToWait) {
        int penaltyThreshold = Math.max(1, properties.getPenaltyThreshold());

        Integer nextCount = jdbcTemplate.queryForObject("""
                INSERT INTO rate_limit_violation (bucket_key, violation_count, updated_at)
                VALUES (?, 1, CURRENT_TIMESTAMP)
                ON CONFLICT (bucket_key)
                DO UPDATE SET
                    violation_count = rate_limit_violation.violation_count + 1,
                    updated_at = CURRENT_TIMESTAMP
                RETURNING violation_count
                """, Integer.class, bucketKey);

        if (nextCount == null || nextCount < penaltyThreshold) {
            return baseSecondsToWait;
        }

        long multiplier = (long) Math.pow(2, Math.min(nextCount - penaltyThreshold, MAX_EXPONENT));

        long penaltySeconds = Math.max(1, baseSecondsToWait * multiplier);
        Instant penaltyUntil = Instant.now().plusSeconds(penaltySeconds);

        jdbcTemplate.update("""
                UPDATE rate_limit_violation
                SET penalty_until = ?, updated_at = CURRENT_TIMESTAMP
                WHERE bucket_key = ?
                """, Timestamp.from(penaltyUntil), bucketKey);

        return penaltySeconds;
    }

    public void resetViolations(Long bucketKey) {
        jdbcTemplate.update("""
                DELETE FROM rate_limit_violation
                WHERE bucket_key = ?
                """, bucketKey);
    }

    private void saveViolationWithoutPenalty(Long bucketKey, int violationCount) {
        jdbcTemplate.update("""
                INSERT INTO rate_limit_violation (bucket_key, violation_count, penalty_until, updated_at)
                VALUES (?, ?, NULL, CURRENT_TIMESTAMP)
                ON CONFLICT (bucket_key)
                DO UPDATE SET
                    violation_count = EXCLUDED.violation_count,
                    penalty_until = NULL,
                    updated_at = CURRENT_TIMESTAMP
                """, bucketKey, violationCount);
    }
}
