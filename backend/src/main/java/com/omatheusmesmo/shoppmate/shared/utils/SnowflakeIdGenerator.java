package com.omatheusmesmo.shoppmate.shared.utils;

import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {

    // Base epoch: January 1, 2024 (in milliseconds)
    private static final long CUSTOM_EPOCH = 1704067200000L;

    // Bit allocations for each component of the ID
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    // Maximum sequence value: 4095. Used as a bitmask to prevent sequence overflow.
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS; // Shifts left by 12 bits
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // Shifts left by 22 bits

    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator() {
        this.workerId = 1L;
    }

    // 'synchronized' prevents race conditions if multiple threads request an ID simultaneously.
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        // Safety check: Prevents duplicate IDs if the server clock synchronizes backwards (e.g., NTP adjustment).
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id.");
        }

        // If multiple IDs are requested within the exact same millisecond:
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;

            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}