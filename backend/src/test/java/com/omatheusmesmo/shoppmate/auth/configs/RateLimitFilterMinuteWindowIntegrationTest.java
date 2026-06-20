package com.omatheusmesmo.shoppmate.auth.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = { "security.rate-limit.capacity=2", "security.rate-limit.refill-tokens=2",
        "security.rate-limit.refill-duration=PT1M",

        "security.rate-limit.short-burst-capacity=100", "security.rate-limit.short-burst-refill-tokens=100",
        "security.rate-limit.short-burst-refill-duration=PT0.5S",

        "security.rate-limit.enabled-methods[0]=PUT", "security.rate-limit.included-paths[0]=/unit",
        "security.rate-limit.included-paths[1]=/unit/**" })
class RateLimitFilterMinuteWindowIntegrationTest extends BaseRateLimitFilterWindowingIntegrationTest {

    @Test
    @DisplayName("Blocks request when minute window capacity is exhausted")
    void shouldBlockRequestWhenMinuteWindowCapacityIsExhausted() throws Exception {
        String ipAddress = "10.0.2.1";

        performAllowedPut(ipAddress);
        performAllowedPut(ipAddress);

        performBlockedPut(ipAddress);
    }
}
