package com.omatheusmesmo.shoppmate.auth.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.omatheusmesmo.shoppmate.shared.domain.BaseAuditableEntity;

@Entity
@Table(name = "rate_limit_violation")
@Getter
@Setter
@NoArgsConstructor
public class RateLimitViolation extends BaseAuditableEntity {

    @Column(name = "bucket_key", nullable = false, unique = true)
    private Long bucketKey;
    @Column(name = "violation_count", nullable = false)
    private Integer violationCount;

    @Column(name = "penalty_until")
    private Instant penaltyUntil;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public static RateLimitViolation firstViolation(Long bucketKey) {
        RateLimitViolation violation = new RateLimitViolation();
        violation.setBucketKey(bucketKey);
        violation.setViolationCount(1);
        return violation;
    }

    public void incrementViolationCount() {
        this.violationCount = this.violationCount + 1;
    }
}
