package com.omatheusmesmo.shoppmate.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.omatheusmesmo.shoppmate.auth.entity.RateLimitViolation;

@Repository
public interface RateLimitViolationRepository extends JpaRepository<RateLimitViolation, Long> {

    Optional<RateLimitViolation> findByBucketKeyAndDeletedFalse(Long bucketKey);
}
