package com.omatheusmesmo.shoppmate.auth.configs;

import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "security.rate-limit.capacity=1",
        "security.rate-limit.refill-tokens=1",
        "security.rate-limit.refill-duration=PT5S",
        "security.rate-limit.short-burst-enabled=false",
        "security.rate-limit.enabled-methods[0]=POST",
        "security.rate-limit.included-paths[0]=/auth/**"
})
class RateLimitPenaltyIntegrationTest extends AbstractIntegrationTest {

    private static final String TEST_CLIENT_IP = "203.0.113.50";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBucketsAndViolations() {
        jdbcTemplate.update("DELETE FROM bucket");
        jdbcTemplate.update("DELETE FROM rate_limit_violation");
    }

    @Test
    void shouldStorePenaltyAndBlockRepeatedRequestsDuringActivePenaltyWindow() throws Exception {
        // First request consumes the only available token.
        // It may return 404 because the fake user does not exist, but it should not be rate-limited yet.
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginPayload())
                        .with(request -> {
                            request.setRemoteAddr(TEST_CLIENT_IP);
                            return request;
                        }))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(429));

        // First blocked request creates the penalty record.
        int firstRetryAfter = performBlockedLoginAndReturnRetryAfter();

        Integer violationCount = jdbcTemplate.queryForObject(
                "SELECT violation_count FROM rate_limit_violation",
                Integer.class
        );

        assertThat(violationCount).isEqualTo(1);

        // Second blocked request is blocked by the active penalty window.
        // Retry-After can be lower because it is now showing the remaining penalty time.
        int secondRetryAfter = performBlockedLoginAndReturnRetryAfter();

        assertThat(firstRetryAfter).isGreaterThan(0);
        assertThat(secondRetryAfter).isGreaterThan(0);
        assertThat(secondRetryAfter).isLessThanOrEqualTo(firstRetryAfter);
    }

    private int performBlockedLoginAndReturnRetryAfter() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginPayload())
                        .with(request -> {
                            request.setRemoteAddr(TEST_CLIENT_IP);
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andReturn();

        return Integer.parseInt(result.getResponse().getHeader("Retry-After"));
    }

    private String loginPayload() {
        return """
                {
                  "email": "wrong@test.com",
                  "password": "wrong-password"
                }
                """;
    }
}