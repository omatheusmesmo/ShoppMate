package com.omatheusmesmo.shoppmate.auth.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@TestPropertySource(properties = { "security.rate-limit.enabled=true",

        // Long/minute window: intentionally high so it does NOT block this test.
        "security.rate-limit.capacity=100", "security.rate-limit.refill-tokens=100",
        "security.rate-limit.refill-duration=1m",

        // Short burst window: this is what this test is proving.
        "security.rate-limit.short-burst-enabled=true", "security.rate-limit.short-burst-capacity=2",
        "security.rate-limit.short-burst-refill-tokens=2", "security.rate-limit.short-burst-refill-duration=500ms",

        // Scope the filter to the endpoint/method used by the test.
        "security.rate-limit.included-paths[0]=/unit", "security.rate-limit.included-paths[1]=/unit/**",
        "security.rate-limit.enabled-methods[0]=PUT" })
class RateLimitFilterShortBurstWindowIntegrationTest extends BaseRateLimitFilterWindowingIntegrationTest {

    @Test
    @DisplayName("Blocks rapid requests when short burst window capacity is exhausted")
    void shouldBlockRapidRequestWhenShortBurstWindowCapacityIsExhausted() throws Exception {
        String ipAddress = "10.0.4.1";

        performAllowedPut(ipAddress);
        performAllowedPut(ipAddress);

        performBlockedPut(ipAddress);
    }

    @Test
    @DisplayName("Allows request after short burst window refills")
    void shouldAllowRequestAfterShortBurstWindowRefills() throws Exception {
        String ipAddress = "10.0.4.2";

        performAllowedPut(ipAddress);
        performAllowedPut(ipAddress);

        performBlockedPut(ipAddress);

        Thread.sleep(600);

        performAllowedPut(ipAddress);
    }
}
