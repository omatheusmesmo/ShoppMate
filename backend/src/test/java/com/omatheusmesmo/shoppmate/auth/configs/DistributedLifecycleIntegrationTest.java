package com.omatheusmesmo.shoppmate.auth.configs;

import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@TestPropertySource(properties = {
        "security.rate-limit.capacity=2",
        "security.rate-limit.refill-tokens=2",
        "security.rate-limit.refill-duration=PT1S",
        "security.rate-limit.short-burst-enabled=false",
        "security.rate-limit.enabled-methods[0]=POST",
        "security.rate-limit.included-paths[0]=/auth/**"
})
class DistributedLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RateLimitProperties properties;

    @Autowired
    private RateLimitViolationTracker violationTracker;

    @Autowired
    private ProxyManager<Long> proxyManager;

    private RateLimitFilter nodeAServer;
    private RateLimitFilter nodeBServer;

    @BeforeEach
    void setUp() {
        // Separate filter instances simulate separate app nodes sharing the same distributed bucket store.
        this.nodeAServer = new RateLimitFilter(properties, proxyManager, violationTracker);
        this.nodeBServer = new RateLimitFilter(properties, proxyManager, violationTracker);
    }

    @Test
    void shouldShareRateLimitBucketAcrossSeparateFilterInstancesAndRecoverAfterRefill() throws Exception {
        String testClientIp = "198.51.100.75";
        int capacity = properties.getCapacity();

        // Node A consumes the full shared bucket for this client.
        for (int i = 0; i < capacity; i++) {
            MockHttpServletRequest request = createMockRequest(testClientIp, "POST", "/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();

            nodeAServer.doFilter(request, response, new MockFilterChain());

            assertEquals(200, response.getStatus());
        }

        // Node B should see the same shared bucket as exhausted.
        MockHttpServletRequest blockedRequest = createMockRequest(testClientIp, "POST", "/auth/login");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        nodeBServer.doFilter(blockedRequest, blockedResponse, new MockFilterChain());

        assertEquals(429, blockedResponse.getStatus());
        assertNotNull(blockedResponse.getHeader("Retry-After"));

        // Wait for the shared bucket to refill before retrying.
        long secondsToWait = Long.parseLong(blockedResponse.getHeader("Retry-After"));
        Thread.sleep((secondsToWait * 1000) + 100);

        // After refill, Node B should allow the same client again.
        MockHttpServletRequest recoveryRequest = createMockRequest(testClientIp, "POST", "/auth/login");
        MockHttpServletResponse recoveryResponse = new MockHttpServletResponse();

        nodeBServer.doFilter(recoveryRequest, recoveryResponse, new MockFilterChain());

        assertEquals(200, recoveryResponse.getStatus());
    }

    private MockHttpServletRequest createMockRequest(String ip, String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        request.setMethod(method);
        request.setRequestURI(uri);
        return request;
    }
}