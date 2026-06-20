package com.omatheusmesmo.shoppmate.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;

import com.omatheusmesmo.shoppmate.auth.configs.RateLimitProperties;
import com.omatheusmesmo.shoppmate.auth.entity.RateLimitViolation;
import com.omatheusmesmo.shoppmate.auth.repository.RateLimitViolationRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;

@Service
@RequiredArgsConstructor
public class RateLimitViolationTracker {

    private static final int MAX_EXPONENT = 5;
    private static final int MAX_RETRIES = 3;

    private final RateLimitViolationRepository rateLimitViolationRepository;
    private final RateLimitProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final AuditService auditService;

    public Optional<Long> getActivePenaltySeconds(Long bucketKey) {
        return rateLimitViolationRepository.findByBucketKeyAndDeletedFalse(bucketKey)
                .map(RateLimitViolation::getPenaltyUntil).filter(penaltyUntil -> penaltyUntil.isAfter(Instant.now()))
                .map(penaltyUntil -> Duration.between(Instant.now(), penaltyUntil).toSeconds())
                .map(seconds -> Math.max(1L, seconds));
    }

    public long recordViolationAndCalculatePenaltySeconds(Long bucketKey, long baseSecondsToWait) {
        return executeWithOptimisticRetry(() -> transactionTemplate.execute(status -> {
            int penaltyThreshold = Math.max(1, properties.getPenaltyThreshold());

            RateLimitViolation violation = rateLimitViolationRepository.findByBucketKeyAndDeletedFalse(bucketKey)
                    .orElseGet(() -> RateLimitViolation.firstViolation(bucketKey));

            boolean isNew = violation.getId() == null;

            if (!isNew) {
                violation.incrementViolationCount();
            }

            int nextCount = violation.getViolationCount();
            long secondsToWait = baseSecondsToWait;

            if (nextCount >= penaltyThreshold) {
                long multiplier = (long) Math.pow(2, Math.min(nextCount - penaltyThreshold, MAX_EXPONENT));
                secondsToWait = Math.max(1L, baseSecondsToWait * multiplier);

                violation.setPenaltyUntil(Instant.now().plusSeconds(secondsToWait));
            }

            auditService.setAuditData(violation, isNew);
            rateLimitViolationRepository.saveAndFlush(violation);

            return secondsToWait;
        }));
    }

    public void resetViolations(Long bucketKey) {
        executeWithOptimisticRetry(() -> transactionTemplate.execute(status -> {
            rateLimitViolationRepository.findByBucketKeyAndDeletedFalse(bucketKey)
                    .ifPresent(rateLimitViolationRepository::delete);

            rateLimitViolationRepository.flush();

            return null;
        }));
    }

    private <T> T executeWithOptimisticRetry(RetryableOperation<T> operation) {
        int attempt = 0;

        while (true) {
            try {
                return operation.execute();
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException exception) {
                attempt++;

                if (attempt >= MAX_RETRIES) {
                    throw exception;
                }
            }
        }
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute();
    }
}
